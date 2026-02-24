export const permissionApi = {
  hasPermission(userPermissions, permission) {
    return userPermissions && userPermissions.includes(permission);
  },
  /**
   * 检查用户是否拥有多个权限中的任意一个（逻辑或 OR）
   * @param {Array} userPermissions - 用户拥有的权限数组
   * @param {Array} permissions - 要检查的权限数组
   * @returns {boolean}
   */
  hasAnyPermission(userPermissions, permissions) {
    // 如果用户权限或传入的权限数组为空，直接返回 false
    if (!userPermissions || !permissions || permissions.length === 0) {
      return false;
    }
    // 使用 some() 方法，只要有一个权限匹配上，就返回 true
    return permissions.some((perm) => userPermissions.includes(perm));
  },
};
