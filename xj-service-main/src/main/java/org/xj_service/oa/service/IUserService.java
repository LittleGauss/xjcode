package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.entity.UserRole;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUserService extends IService<User> {
    User findByUsername(String username);
    List<String> getUserRoleCodes(Integer userId);
    Set<String> getUserPermissions(Integer userId);
    /** 获取用户所拥有的权限名称列表 */
    List<String> getUserPermissionNames(Integer userId);
    // 添加获取完整用户信息的方法
    Map<String, Object> getUserInfoWithRolesAndPermissions(Integer userId);
    // 新增的方法
    void exportUsers(OutputStream outputStream) throws IOException;
    void importUsers(MultipartFile file) throws IOException;

    boolean saveUserRole(UserRole userRole);

    boolean isAdmin(Integer operatorId);
    /**
     * 根据角色名称获取用户列表
     * @param codeNames 角色名称
     * @return 用户列表
     */
    List<User> getUsersByRoleCode(List<String> codeNames);

    /**
     * 更新用户的角色关系（覆盖式）
     */
    void updateUserRoles(Integer userId, java.util.List<Integer> roleIds);
	public boolean changeUserPassword(Integer userId, String oldPassword, String newPassword);
    /** 根据用户ID获取用户部门的主管 */
    List<User> getDepartmentUser(Integer userId);
    /** 根据物资类别获取审核人 不同类别人不一样 包括自己主管 */
    List getFirstApprover(Integer itemCategory);
//根据用户id获取用户的部门信息
    User getUserWithDepartment(Integer userId);
}
