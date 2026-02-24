import Cookies from "js-cookie";

const TokenKey = "loginToken";
const LoginUser = "login_user";

// 优先使用 Cookie（便于后端和前端统一），但同时写入 localStorage 以提高在特殊环境下的恢复能力
export function getToken() {
  const c = Cookies.get(TokenKey);
  if (c) return c;
  try {
    return window.localStorage.getItem(TokenKey);
  } catch (e) {
    return undefined;
  }
}

export function getUserToken() {
  try {
    const stored = JSON.parse(window.localStorage.getItem(LoginUser));
    // 校验是否过期（存储结构为 { value: 原数据, expiry: 过期时间戳 }）
    if (stored?.expiry && Date.now() > stored.expiry) {
      window.localStorage.removeItem(LoginUser); // 过期则删除
      return undefined;
    }
    return stored?.value; // 返回实际token值
  } catch (e) {
    return undefined;
  }
}
export function setUserToken(token) {
  try {
    // 设置过期时间（示例：1小时，可根据需求调整，单位毫秒）
    // const expiry = Date.now() + 3600 * 1000; // 1小时后过期
    const expiry = Date.now() + 1800 * 1000; // 30分钟后过期
    const serializedValue = JSON.stringify({
      value: token, // 存储实际token
      expiry, // 过期时间戳
    });
    window.localStorage.setItem(LoginUser, serializedValue);
  } catch (e) {
    console.debug("setUserToken: localStorage.setItem failed", e);
  }
  return token;
}

// 续期token的过期时间（比如每次路由跳转后延期30分钟）
export function renewUserTokenExpiry(ttl = 1800 * 1000) {
  try {
    const stored = JSON.parse(window.localStorage.getItem(LoginUser));
    if (stored) {
      // 重新设置过期时间（比如再延长ttl）
      stored.expiry = Date.now() + ttl;
      window.localStorage.setItem(LoginUser, JSON.stringify(stored));
      return true;
    }
    return false; // token不存在则返回false
  } catch (e) {
    console.debug("renewUserTokenExpiry failed", e);
    return false;
  }
}
export function removeUserToken() {
  try {
    window.localStorage.removeItem(LoginUser);
  } catch (e) {
    /* istanbul ignore next */
    console.debug("removeToken: localStorage.removeItem failed", e);
  }
}

export function setToken(token) {
  // 写入 cookie（默认 path=/）
  try {
    Cookies.set(TokenKey, token);
  } catch (e) {
    // ignore cookie set failure in some restricted environments
    /* istanbul ignore next */
    console.debug("setToken: Cookies.set failed", e);
  }
  try {
    window.localStorage.setItem(TokenKey, token);
  } catch (e) {
    // ignore localStorage failure (e.g., in private mode)
    /* istanbul ignore next */
    console.debug("setToken: localStorage.setItem failed", e);
  }
  return token;
}

export function removeToken() {
  try {
    Cookies.remove(TokenKey);
  } catch (e) {
    /* istanbul ignore next */
    console.debug("removeToken: Cookies.remove failed", e);
  }
  try {
    window.localStorage.removeItem(TokenKey);
  } catch (e) {
    /* istanbul ignore next */
    console.debug("removeToken: localStorage.removeItem failed", e);
  }
}
