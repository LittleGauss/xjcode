/**
 * 校验密码强度
 * @param {string} password 密码
 * @returns {boolean} 是否符合要求
 */
export function validatePassword(password) {
  // 至少8位
  if (password.length < 8) {
    return false;
  }

  // 包含两种及以上字符类型（数字、小写字母、大写字母、特殊字符）
  const hasNumber = /\d/.test(password);
  const hasLower = /[a-z]/.test(password);
  const hasUpper = /[A-Z]/.test(password);
  const hasSpecial = /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password);

  return hasNumber && hasLower && hasUpper && hasSpecial;
}

/**
 * 校验用户名格式
 * @param {string} username 用户名
 * @returns {boolean} 是否符合要求
 */
export function validateUsername(username) {
  // 3-20位字母数字下划线
  return /^[a-zA-Z0-9_]{3,20}$/.test(username);
}
