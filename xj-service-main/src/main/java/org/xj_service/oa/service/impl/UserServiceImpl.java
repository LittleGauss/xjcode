package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.entity.*;
import org.xj_service.oa.mapper.*;
import org.xj_service.oa.service.IUserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.utils.PasswordUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private PasswordUtil passwordUtil; // 密码工具类




    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    @Override
    public List<String> getUserRoleCodes(Integer userId) {
        return userRoleMapper.selectRoleCodesByUserId(userId);
    }


    @Override
    public Map<String, Object> getUserInfoWithRolesAndPermissions(Integer userId) {
        Map<String, Object> userInfo = new HashMap<>();

        // 获取用户基本信息
        User user = getById(userId);
        userInfo.put("user", user);

        // 获取用户角色
        List<Role> roles = getRolesByUserId(userId);
        userInfo.put("roles", roles);

        // 获取用户权限
        Set<String> permissions = getUserPermissions(userId);
        userInfo.put("permissions", permissions);

        return userInfo;
    }

    /**
     * 根据用户ID获取角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<Role> getRolesByUserId(Integer userId) {
        // 先通过用户ID获取角色ID列表
        QueryWrapper<UserRole> userRoleQueryWrapper = new QueryWrapper<>();
        userRoleQueryWrapper.eq("user_id", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleQueryWrapper);

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取角色ID集合
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        // 根据角色ID获取角色信息
        QueryWrapper<Role> roleQueryWrapper = new QueryWrapper<>();
        roleQueryWrapper.in("id", roleIds);
        roleQueryWrapper.orderByDesc("id");
        return roleMapper.selectList(roleQueryWrapper);
    }

    @Override
    public Set<String> getUserPermissions(Integer userId) {
        Set<String> permissions = new HashSet<>();

        // 获取用户角色
        List<Role> roles = getRolesByUserId(userId);
        if (roles.isEmpty()) {
            return permissions;
        }

        // 获取角色ID集合
        List<Integer> roleIds = roles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());

        // 获取角色权限关联信息
        QueryWrapper<RolePermission> rolePermissionQueryWrapper = new QueryWrapper<>();
        rolePermissionQueryWrapper.in("role_id", roleIds);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermissionQueryWrapper);

        if (rolePermissions.isEmpty()) {
            return permissions;
        }

        // 获取权限ID集合
        List<Integer> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 根据权限ID获取权限编码
        QueryWrapper<Permission> permissionQueryWrapper = new QueryWrapper<>();
        permissionQueryWrapper.in("id", permissionIds);
        List<Permission> permissionList = permissionMapper.selectList(permissionQueryWrapper);

        // 收集权限编码
        permissionList.forEach(permission -> {
            if (permission.getCode() != null) {
                permissions.add(permission.getCode());
            }
        });

        return permissions;
    }

    /**
     * 获取用户拥有的权限名称列表
     */
    @Override
    public List<String> getUserPermissionNames(Integer userId) {
        List<String> permissionNames = new ArrayList<>();
        List<Role> roles = getRolesByUserId(userId);
        if (roles.isEmpty()) {
            return permissionNames;
        }
        List<Integer> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        QueryWrapper<RolePermission> rpQw = new QueryWrapper<>();
        rpQw.in("role_id", roleIds);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rpQw);
        if (rolePermissions.isEmpty()) {
            return permissionNames;
        }
        List<Integer> permissionIds = rolePermissions.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        QueryWrapper<Permission> pQw = new QueryWrapper<>();
        pQw.in("id", permissionIds);
        List<Permission> permissionList = permissionMapper.selectList(pQw);
        permissionList.forEach(p -> { if (p.getName() != null) { permissionNames.add(p.getName()); } });
        return permissionNames;
    }

    @Override
    public void updateUserRoles(Integer userId, List<Integer> roleIds) {
        // 先删除原有关联
        QueryWrapper<UserRole> del = new QueryWrapper<>();
        del.eq("user_id", userId);
        userRoleMapper.delete(del);

        // 支持多个角色：遍历roleIds数组，逐个插入用户角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Integer roleId : roleIds) { // 遍历所有roleId
                if (roleId != null) {
                    UserRole ur = new UserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    userRoleMapper.insert(ur); // 逐个插入关联记录
                }
            }
        }
    }

    /**
     * 导出用户
     * @param outputStream HTTP响应对象
     */
    @Override
    public void exportUsers(OutputStream outputStream) throws IOException {
        try {
            List<User> userList = list();

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("用户列表");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "用户名", "昵称", "邮箱", "手机号", "状态", "入职时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            for (int i = 0; i < userList.size(); i++) {
                User user = userList.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getNickname());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getPhone());
                row.createCell(5).setCellValue(user.getStatus() == 1 ? "正常" : "禁用");
                row.createCell(6).setCellValue(user.getCreatedAt() != null ?
                        user.getCreatedAt().toString() : "");
            }

            // 写入响应流
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (Exception e) {
            throw new IOException("导出用户数据失败", e);
        }
    }

    @Override
    public void importUsers(MultipartFile file) throws IOException {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // 跳过表头，从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                User user = new User();

                // 读取单元格数据
                Cell usernameCell = row.getCell(1);
                Cell nicknameCell = row.getCell(2);
                Cell emailCell = row.getCell(3);
                Cell phoneCell = row.getCell(4);
                Cell statusCell = row.getCell(5);
                Cell creatAtCell = row.getCell(6);

                if (usernameCell != null) {
                    user.setUsername(getCellValueAsString(usernameCell));
                }

                if (nicknameCell != null) {
                    user.setNickname(getCellValueAsString(nicknameCell));
                }

                if (emailCell != null) {
                    user.setEmail(getCellValueAsString(emailCell));
                }

                if (phoneCell != null) {
                    user.setPhone(getCellValueAsString(phoneCell));
                }

                if (statusCell != null) {
                    String statusStr = getCellValueAsString(statusCell);
                    user.setStatus("正常".equals(statusStr) ? 1 : 0);
                }
                // ========== 处理入职时间 ==========
                if (creatAtCell != null) {
                    LocalDateTime createdAt = null; // 初始化入职时间变量
                    // 先判断单元格类型，针对性处理日期
                    if (creatAtCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(creatAtCell)) {
                        // 场景1：Excel中单元格是「日期格式」（存储为数字，需转换为Date再转LocalDateTime）
                        Date dateValue = creatAtCell.getDateCellValue();
                        // Date 转 LocalDateTime（指定时区，避免时间偏移）
                        createdAt = dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    } else {
                        // 场景2：Excel中单元格是「字符串格式」（手动填写的 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
                        String createdAtStr = getCellValueAsString(creatAtCell).trim();
                        if (!StringUtils.isEmpty(createdAtStr)) {
                            try {
                                // 兼容两种常见日期格式，自动匹配
                                DateTimeFormatter formatter = createdAtStr.contains(" ")
                                        ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        : DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                createdAt = LocalDateTime.parse(createdAtStr, formatter);
                            } catch (DateTimeException e) {

                            }
                        }
                    }
                    // 给User对象赋值入职时间（非空才赋值，避免覆盖为null）
                    if (createdAt != null) {
                        user.setCreatedAt(createdAt);
                    }
                }

                // 设置默认密码和其他字段
                user.setPassword(passwordUtil.encrypt("123456")); // 默认密码
//                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());

                // 保存用户（避免重复用户名）
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("username", user.getUsername());
                if (getOne(queryWrapper) == null) {
                    save(user);
                }
            }

            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("导入用户数据失败", e);
        }
    }

    // 或者使用通用的处理方法
    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                // 判断是否为日期格式
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }



    @Override
    public boolean save(User user) {
        return super.save(user);
    }


    @Override
    public boolean saveUserRole(UserRole userRole) {
        return userRoleMapper.insert(userRole) > 0;
    }

    @Override
    public boolean isAdmin(Integer operatorId) {
        if (operatorId == null) {
            return false;
        }

        // 获取用户角色编码列表
        List<String> roleCodes = getUserRoleCodes(operatorId);

        // 检查是否包含超级管理员角色
        return roleCodes.contains("SUPER_ADMIN");
    }

    public boolean changeUserPassword(Integer userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 2. 校验密码（自动兼容明文/密文）
        boolean passwordCorrect = passwordUtil.checkPassword(oldPassword, user.getPassword());
        if (!passwordCorrect) {
            return false;
        }
        // 3. 明文校验新密码与旧密码是否一致
        if (newPassword.equals(user.getPassword())) {
            return false;
        }
        // 4. 密码加密（明文 → BCrypt 密文）
        String encryptedPassword = passwordUtil.encrypt(newPassword);
        // 5. 直接存储新密码（密文）
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(encryptedPassword); // 密文存储
        boolean updateSuccess = userMapper.updateById(updateUser) > 0;
        return updateSuccess;
    }

    @Override
    public List<User> getUsersByRoleCode(List<String> codeNames) {
        return userMapper.getUsersByRoleCode(codeNames);
    }

    /** 根据用户ID获取用户部门的主管 */
    @Override
    public List<User> getDepartmentUser(Integer userId){
        return userMapper.getDepartmentUser(userId);
    }

    /**
     * 电子耗材 → 信息中心审批
     * 实验耗材 → 质量中心审批
     * 其他耗材 → 部门负责人审批
     * '1', '办公用品',
     * '2', '电子耗材',
     * '3', '实验耗材',
     * '4', '清洁用品',
     * '5', '其他物资',
     * @param itemCategory
     * @return
     */
    @Override
    public List<User> getFirstApprover(Integer itemCategory) {
        String roleCode = "ROLE_DEPT_MANAGER";
        switch (itemCategory) {
            case 2:
                //信息中心审批  返回信息中心的审批人 id + name  ROLE_IT_CENTER
                roleCode = "ROLE_IT_CENTER";
                break;
            case 3:
                //质量中心审批审批  返回质量中心审批的审批人 id + name
                roleCode = "ROLE_QUALITY_CENTER";
                break;
            default:
                roleCode = "ROLE_DEPT_MANAGER";
                // 返回部门主管的审批人 id + name ROLE_DEPT_MANAGER
        }
        return userMapper.getUsersByRoleCode(Collections.singletonList(roleCode));
    }

    @Override
    public User getUserWithDepartment(Integer userId) {
        return userMapper.getUserWithDepartment(userId);
    }

}
