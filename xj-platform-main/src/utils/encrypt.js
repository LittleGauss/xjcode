import JSEncrypt from "jsencrypt";

// Vue2 读取环境变量：process.env.VUE_APP_XXX（必须带 VUE_APP_ 前缀）
const PUBLIC_KEY = process.env.VUE_APP_RSA_PUBLIC_KEY;
// 判断是否是生产环境（Vue2 用 process.env.NODE_ENV）
const IS_PROD = process.env.NODE_ENV === "production";

// 创建 RSA 加密实例
const rsa = new JSEncrypt();
rsa.setPublicKey(PUBLIC_KEY);

/**
 * 密码加密：生产环境 RSA 加密，开发环境直接返回明文
 * @param {String} password 明文密码
 * @returns {Object} { password: 加密后密码, isEncrypted: 是否加密 }
 */
export function encryptPassword(password) {
  if (IS_PROD) {
    // 生产环境：RSA 加密
    const encryptedPwd = rsa.encrypt(password);
    return {
      password: encryptedPwd,
      isEncrypted: true, // 告诉后端这是加密后的密码
    };
  } else {
    // 开发环境：RSA 加密测试
    const encryptedPwd = rsa.encrypt(password);
    return {
      password: encryptedPwd,
      isEncrypted: true,
    };
  }
}
