package org.xj_service.oa.common;

public interface Constants {
    String CODE_200="200";   //成功
    String CODE_500="500";   //系统错误
    String CODE_401="401";   //未登录
    String CODE_400="400";   //参数不足 验证码错误
    String CODE_600="600";   //其他业务异常

    String DICT_TYPE_ICON = "icon";

    // 验证码相关常量
    String CAPTCHA_KEY = "captcha_key";  // 验证码在Redis中的key前缀
    int CAPTCHA_EXPIRE = 300;  // 验证码过期时间(秒)
}
