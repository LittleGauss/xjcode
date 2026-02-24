package org.xj_service.oa.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 简化版 RSA 密钥对生成器
 */
public class RsaKeyGenerator {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // 生成密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 获取私钥字节（PKCS8格式）
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

        // 获取公钥字节
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

        // Base64编码
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);

        // 格式化输出
        System.out.println("=================== RSA 私钥 ===================");
        System.out.println("-----BEGIN PRIVATE KEY-----");
        // 每64个字符换行
        for (int i = 0; i < privateKeyBase64.length(); i += 64) {
            int end = Math.min(privateKeyBase64.length(), i + 64);
            System.out.println(privateKeyBase64.substring(i, end));
        }
        System.out.println("-----END PRIVATE KEY-----");

        System.out.println("\n=================== RSA 公钥 ===================");
        System.out.println("-----BEGIN PUBLIC KEY-----");
        for (int i = 0; i < publicKeyBase64.length(); i += 64) {
            int end = Math.min(publicKeyBase64.length(), i + 64);
            System.out.println(publicKeyBase64.substring(i, end));
        }
        System.out.println("-----END PUBLIC KEY-----");

        // 也输出一行版本（用于配置文件）
        System.out.println("\n=================== 单行版本（用于配置文件）===================");
        System.out.println("私钥: " + privateKeyBase64);
        System.out.println("公钥: " + publicKeyBase64);
    }
}