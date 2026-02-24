package org.xj_service.oa.config.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import org.xj_service.oa.common.Constants;
import org.xj_service.oa.common.RequirePermission;
import org.xj_service.oa.common.RequireRole;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.exception.ServiceException;
import org.xj_service.oa.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只处理方法级别的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // 如果方法上没有注解，则检查类上的注解
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 检查权限注解
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }

        // 如果没有权限注解和角色注解，则放行
        if (requireRole == null && requirePermission == null) {
            return true;
        }

        // 获取token并验证用户身份
        String token = request.getHeader("Authorization");
        System.out.println("peimission拦截到请求"+token);

        if (StrUtil.isBlank(token)) {
            throw new ServiceException(Constants.CODE_401, "无token，请重新登录");
        }

        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (Exception e) {
            throw new ServiceException(Constants.CODE_401, "token验证失败");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new ServiceException(Constants.CODE_401, "用户不存在，请重新登录");
        }
        // 角色检查
        if (requireRole != null) {
            List<String> userRoles = userService.getUserRoleCodes(Integer.valueOf(userId));
            String[] requiredRoles = requireRole.value();
//            System.out.println("用户角色列表: " + userRoles);
//            System.out.println("要求角色列表: " + Arrays.toString(requiredRoles));
            if (requiredRoles.length == 0) {
                return true;
            }

            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    return true;
                }
            }
        }
        // 权限检查
        if (requirePermission != null) {
            Set<String> userPermissions = userService.getUserPermissions(Integer.valueOf(userId));
            String[] requiredPermissions = requirePermission.value();
//            System.out.println("用户权限列表: " + userPermissions);
//            System.out.println("要求权限列表: " + Arrays.toString(requiredPermissions));
            if (requiredPermissions.length == 0) {
                return true;
            }

            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    return true;
                }
            }
        }

        throw new ServiceException(Constants.CODE_401, "权限不足，无法访问该资源");
    }
}
