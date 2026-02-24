import request from "@/utils/request";

// 用户相关接口
export const userApi = {
  // 获取用户列表
  getUserList(params) {
    return request.get("/api/user/list", { params });
  },

  // 获取用户详情
  getUserDetails(id) {
    return request.get(`/user/detail/${id}`);
  },

  // 获取用户部门详细
  getUserDepartmentInfo(id) {
    return request.get(`/user/user-department/${id}`);
  },

  getUserById(id) {
    return request.get(`/user/${id}`);
  },

  // 新增用户
  addUser(data) {
    return request.post("/api/user", data);
  },

  // 编辑用户
  updateUser(id, data) {
    return request.put(`/api/user/${id}`, data);
  },
  // 更新用户角色
  updateUserRoles(id, roleIds) {
    const flatRoleIds =
      Array.isArray(roleIds) && roleIds.length > 0 ? roleIds[0] : [];
    return request.put(`/api/user/${id}/roles`, { roleIds: flatRoleIds });
  },

  // 删除用户
  deleteUser(id) {
    return request.delete(`/api/user/${id}`);
  },

  // 切换用户状态
  toggleUserStatus(id, status) {
    return request.put(`/api/user/status/${id}`, { status });
  },

  // 导出用户
  exportUsers() {
    return request.get("/api/user/export");
  },

  // 批量导入用户
  importUsers(formData) {
    return request.post("/api/user/import", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  // 获取部门列表
  getDepartmentList() {
    return request.get("/department/list");
  },
  //根据消耗品类别 选择对应审核人
  getFirstApprover(itemCategory) {
    return request.get(`/user/getFirstApprover/${itemCategory}`);
  },

  // 获取用户的部门主管
  findDepartmentUser(id) {
    return request.get(`/user/findDepartment/${id}`);
  },
  addDepartment(data) {
    return request.post("/department", data);
  },
  getUsersByRole(roleNames) {
    const names = Array.isArray(roleNames) ? roleNames : [roleNames];
    return request.post("/api/user/getUsersByRole", { codeNames: names });
  },
  // 删除部门
  deleteDepartment(id) {
    return request.delete(`/department/${id}`);
  },
  // ========== 新增：管理员重置用户密码接口 ==========
  resetUserPassword(id, passwordInfo) {
    return request.post(`/user/reset-password/${id}`, passwordInfo);
  },
};

// 角色相关接口
export const roleApi = {
  // 获取角色列表
  getRoleList() {
    return request.get("/role/list");
  },

  // 新增角色
  addRole(data) {
    return request.post("/role", data);
  },

  // 更新角色
  updateRole(id, data) {
    return request.put(`/role/${id}`, data);
  },

  // 删除角色
  deleteRole(id) {
    return request.delete(`/role/${id}`);
  },

  // 导出用户
  exportRole() {
    return request.get("/role/export");
  },
};

// 权限相关接口
export const permissionApi = {
  // 获取权限列表
  getPermissionList() {
    return request.get("/permission/list");
  },

  // 分配权限
  assignPermissions(roleId, permissions) {
    return request.post(`/permission/assign/${roleId}`, { permissions });
  },

  // 新增角色
  addPermission(data) {
    return request.post("/permission", data);
  },
};
