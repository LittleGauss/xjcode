package org.xj_service.oa.common;


import java.lang.annotation.*;
/*
自定义注解，主要用于切面日志记录
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 模块名称 */
    String title() default "";

    /** 功能 */
    BusinessType businessType() default BusinessType.OTHER;

    /** 是否保存请求参数 */
    boolean isSaveRequestData() default true;

    /** 是否保存响应参数 */
    boolean isSaveResponseData() default true;
}