package org.xj_service.oa.exception;

import org.xj_service.oa.common.Result;
import org.xj_service.oa.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 如果抛出的的是ServiceException，则调用该方法
     * 
     * @param se 业务异常
     * @return Result
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handle(ServiceException se) {
        return Result.error(se.getCode(), se.getMessage());
    }

    /**
     * 捕获数据库约束违反（例如唯一键冲突），返回友好业务错误信息，避免向前端抛出 500 原始异常
     */
    @ExceptionHandler({ DuplicateKeyException.class, DataIntegrityViolationException.class })
    @ResponseBody
    public Result handleConstraintViolation(Exception ex) {
        logger.warn("Database constraint violation", ex);
        // 返回通用业务错误码和清晰的提示，前端会展示 message
        return Result.error(Constants.CODE_600, "保存数据失败：唯一键冲突（例如车牌号已存在）");
    }
}
