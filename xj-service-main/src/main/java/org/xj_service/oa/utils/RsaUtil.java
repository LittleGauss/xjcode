package org.xj_service.oa.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 解密工具类（专门处理PKCS8格式）
 */
@Component
@ConfigurationProperties(prefix = "app.password.rsa")
public class RsaUtil {

    private String privateKey;
    private String publicKey;

    /**
     * 解密前端传来的 RSA 加密密码
     */
    public String decrypt(String encryptedPassword) {
        try {
            System.out.println("privateKey: " + privateKey);
            // 清理私钥字符串 - 只去除标签和空白
            String cleanPrivateKey = privateKey.replaceAll("-----(BEGIN|END)(.*?)-----", "")
                    .replaceAll("\\s", "");

            System.out.println("清理后的私钥: " + cleanPrivateKey);

            // 使用PKCS8格式解析私钥
            byte[] privateKeyBytes = Base64.getDecoder().decode(cleanPrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            // 解密
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String password = new String(decryptedBytes, StandardCharsets.UTF_8);
            System.out.println("解密成功");
            return password;

        } catch (BadPaddingException e) {
        throw new RuntimeException("解密失败：密文格式错误或私钥不匹配", e);
    } catch (IllegalBlockSizeException e) {
        throw new RuntimeException("解密失败：数据块大小错误", e);
    }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("密码解密失败: " + e.getMessage());
        }


}


    // getter和setter
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}