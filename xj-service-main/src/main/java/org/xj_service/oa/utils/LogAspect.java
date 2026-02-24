package org.xj_service.oa.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.xj_service.oa.entity.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.common.Log;
import org.xj_service.oa.entity.SysOperLog;
import org.xj_service.oa.service.IUserService;
import org.xj_service.oa.service.impl.SysOperLogServiceImpl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    private SysOperLogServiceImpl operLogService;

    @Autowired
    private IUserService userService;

    // 排除不需要序列化的敏感属性
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    // 计算耗时
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new ThreadLocal<>();

    @Before("@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, Log controllerLog) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        SysOperLog operLog = new SysOperLog();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        try {
            operLog.setStatus(0);

            // 设置IP和URL
            if (request != null) {
                operLog.setOperIp(IpUtils.getIpAddr(request));
                operLog.setOperUrl(request.getRequestURI());
                operLog.setRequestMethod(request.getMethod());
            }

            if (e != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 2000 ? e.getMessage().substring(0, 2000) : e.getMessage());
            }

            // 设置耗时
            if (TIME_THREADLOCAL.get() != null) {
                operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
                TIME_THREADLOCAL.remove();
            }

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            operLog.setOperTime(new Date());

            // 处理注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 隔离用户获取逻辑
            String operName = "UNKNOWN";
            try {
                String token = request != null ? request.getHeader("Authorization") : null;
                if (token != null) {
                    // 假设您的 token 就是原始 JWT 字符串
                    String userIdStr = JWT.decode(token).getAudience().get(0);
                    User currentUser = userService.getById(Integer.parseInt(userIdStr));
                    if (currentUser != null) {
                        operName = currentUser.getUsername();
                    }
                }
            } catch (Exception tokenExp) {
                // 如果解析 token 失败，记录警告，但不会中断主日志流程
                log.warn("无法从 Token 中解析用户信息，将记录为 UNKNOWN。错误信息: {}", tokenExp.getMessage());
            }
            operLog.setOperName(operName); // 无论是否成功，都设置操作人
            // 异步保存
            operLogService.saveLog(operLog);
        } catch (Exception exp) {
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }finally {
            // 确保 ThreadLocal 始终被清理
            TIME_THREADLOCAL.remove();
        }
    }

    private void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult) {
        operLog.setBusinessType(log.businessType().ordinal());
        operLog.setTitle(log.title());

        if (log.isSaveRequestData()) {
            setRequestValue(joinPoint, operLog);
        }
        if (log.isSaveResponseData() && jsonResult != null) {
            operLog.setJsonResult(JSON.toJSONString(jsonResult));
        }
    }

    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog) {
        Object[] args = joinPoint.getArgs();
        Object[] arguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // 【关键】过滤掉 Request, Response 和 MinIO 的文件对象
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse || arg instanceof MultipartFile) {
                continue;
            }
            arguments[i] = arg;
        }
        // 序列化时过滤敏感字段
        PropertyPreFilters.MySimplePropertyPreFilter excludeFilter = new PropertyPreFilters().addFilter();
        excludeFilter.addExcludes(EXCLUDE_PROPERTIES);
        String params = JSON.toJSONString(arguments, excludeFilter);
        operLog.setOperParam(params.length() > 2000 ? params.substring(0, 2000) : params);
    }
}