package org.xj_service.oa.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt 密码工具类
 */
@Component
public class PasswordUtil {

    // BCrypt 加密器（强度 10，安全且不耗时）
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // BCrypt 密文的标志：以 $2a$、$2b$、$2y$ 开头
    private static final String BCRYPT_FLAG = "$2";

    /**
     * 明文加密成 BCrypt 密文
     */
    public String encrypt(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 校验密码：明文 vs 密文（自动兼容）
     * @param rawPassword 前端传的明文（可能是 RSA 解密后）
     * @param dbPassword 数据库存的密码（明文或密文）
     */
    public boolean checkPassword(String rawPassword, String dbPassword) {

        if (isEncrypted(dbPassword)) {
//            System.out.println("密码效验isEncrypted:"+ rawPassword + "DB:"+dbPassword);
            // 数据库是密文：用 BCrypt 校验  rawPassword是解密出來的明文  DB是數據庫的密文
            return encoder.matches(rawPassword, dbPassword);
        } else {
//            System.out.println("密码效验明文版:"+ rawPassword + "DB:"+dbPassword);
            // 数据库是明文：直接比对（兼容本地开发）
            return rawPassword.equals(dbPassword);
        }
    }

    /**
     * 判断密码是否已经是 BCrypt 密文
     */
    public boolean isEncrypted(String password) {
        return password != null && password.startsWith(BCRYPT_FLAG);
    }
}