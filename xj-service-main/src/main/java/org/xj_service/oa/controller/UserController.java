package org.xj_service.oa.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

import org.xj_service.oa.common.BusinessType;
import org.xj_service.oa.common.Log;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.common.RequireRole;
import org.xj_service.oa.entity.UserRole;
import org.xj_service.oa.service.IUserService;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.utils.PasswordUtil;
import org.xj_service.oa.utils.RsaUtil;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户管理接口")
public class UserController {

    @Resource
    private IUserService userService;

    // 注入RedisTemplate
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordUtil passwordUtil; // 密码工具类
    @Autowired
    private RsaUtil rsaUtil; // RSA 工具类
    @Value("${app.password.force-rsa-encrypt}")
    private boolean forceRsaEncrypt; // 是否强制 RSA 加密（从配置文件读）

    //根据用户的用户名获取数据库表的这个用户的所有属性
    @GetMapping("/username/{username}")
    public Result findByUsername(@PathVariable String username){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        return Result.success(userService.getOne(queryWrapper));
    }

    /**
     * 用户修改个人信息（手机号/邮箱）
     * @param userInfo 包含手机号、邮箱的用户信息
     * @param request 获取当前登录用户
     * @return 操作结果
     */
    @Log(title = "修改个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/update-info")
    @Operation(summary = "修改个人信息", description = "用户修改自身手机号/邮箱")
    public Result updateUserInfo(@RequestBody Map<String, String> userInfo, HttpServletRequest request) {
        // 1. 参数非空校验
        String phone = userInfo.get("phone");
        String email = userInfo.get("email");
        String username = userInfo.get("username"); // 备用：也可从token解析用户信息

        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("400", "手机号不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            return Result.error("400", "邮箱不能为空");
        }

        // 2. 格式校验
        // 手机号格式校验（11位数字，以1开头）
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error("400", "请输入正确的11位手机号");
        }
        // 邮箱格式校验
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            return Result.error("400", "请输入有效的邮箱地址");
        }

        // 3. Token解析当前登录用户ID（确保只能修改自己的信息）
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty() || !token.contains(".")) {
            return Result.error("401", "请携带有效Token登录后操作");
        }

        Integer userId = null;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            userId = Integer.parseInt(userIdStr);
        } catch (Exception e) {
            return Result.error("401", "Token解析失败，请重新登录");
        }

        try {
            // 4. 校验手机号/邮箱是否被其他用户占用
            // 手机号校验：排除当前用户自己的手机号
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("phone", phone).ne("id", userId);
            if (userService.getOne(phoneQuery) != null) {
                return Result.error("400", "该手机号已被其他账号绑定");
            }

            // 邮箱校验：排除当前用户自己的邮箱
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", email).ne("id", userId);
            if (userService.getOne(emailQuery) != null) {
                return Result.error("400", "该邮箱已被其他账号绑定");
            }

            // 5. 更新用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("404", "用户不存在");
            }
            user.setPhone(phone);
            user.setEmail(email);
            user.setUpdatedAt(LocalDateTime.now()); // 更新时间

            boolean success = userService.updateById(user);
            if (success) {
                return Result.success("个人信息修改成功");
            } else {
                return Result.error("500", "信息修改失败，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "系统异常，信息修改失败");
        }
    }
    /**
     * 获取当前登录用户的权限名称列表
     */
    @GetMapping("/permissions/names")
    @Operation(summary = "获取当前用户权限名称", description = "从Token解析当前用户并返回其拥有的权限名称列表")
    public Result getCurrentUserPermissionNames(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty() || !token.contains(".")) {
            return Result.error("401", "Token缺失或格式错误");
        }
        Integer userId;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            userId = Integer.parseInt(userIdStr);
        } catch (Exception e) {
            return Result.error("401", "Token解析失败");
        }
        try {
            List<String> names = userService.getUserPermissionNames(userId);
            return Result.success(names);
        } catch (Exception e) {
            return Result.error("500", "获取权限名称失败");
        }
    }

    // 新增或修改用户 (仅管理员可操作)
    @Log(title = "添加或修改用户", businessType = BusinessType.INSERT)
    @RequireRole({"SUPER_ADMIN","USER_ADMIN"})
    @Operation(summary = "添加或修改用户", description = "添加或修改用户信息")
    @PostMapping
    public Result save(@RequestBody User user){
        return Result.success(userService.saveOrUpdate(user));
    }

    // 删除用户 (仅管理员可操作)
    @Log(title = "删除用户", businessType = BusinessType.DELETE)
    @RequireRole({"SUPER_ADMIN","USER_ADMIN"})
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    // 批量删除用户 (仅管理员可操作)
    @Log(title = "批量删除用户", businessType = BusinessType.DELETE)
    @RequireRole({"SUPER_ADMIN","USER_ADMIN"})
    @Operation(summary = "批量删除用户", description = "根据ID列表批量删除用户")
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        return Result.success(userService.removeBatchByIds(ids));
    }

    // 查询所有用户 (仅管理员可操作)
    @RequireRole({"SUPER_ADMIN","USER_ADMIN"})
    @Operation(summary = "查询所有用户", description = "查询所有用户信息")
    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    // 根据ID查询用户 
    @Operation(summary = "查询单个用户", description = "根据ID查询单个用户")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    // 分页查询用户 (仅管理员可操作)
    @RequireRole({"SUPER_ADMIN","USER_ADMIN"})
    @Operation(summary = "分页查询用户", description = "分页查询用户信息")
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", username);
        queryWrapper.orderByDesc("id");
        return Result.success(userService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 用户注册
     * @param registerUser 注册用户信息
     * @return 注册结果
     */
    @Log(title = "注册用户", businessType = BusinessType.INSERT)
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public Result register(@RequestBody Map<String, String> registerUser) {
            String username = registerUser.get("username");
            String password = registerUser.get("password");
            String email = registerUser.get("email");
            String phoneNumber = registerUser.get("phoneNumber");
            String departmentIdStr = registerUser.get("departmentId");
            // 参数校验
            if (departmentIdStr == null || username == null || password == null || email == null || phoneNumber == null) {
                return Result.error("400", "所有字段不能为空");
            }


            // 部门ID格式校验（转Long类型）
            Integer departmentId;
            try {
                departmentId = Integer.parseInt(departmentIdStr);
            } catch (NumberFormatException e) {
                return Result.error("400", "部门选择无效，请重新选择");
            }
            // 用户名格式校验
            if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                return Result.error("400", "用户名必须是3-20位的字母、数字或下划线");
            }

            // 邮箱格式校验
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                return Result.error("400", "请输入有效的邮箱地址");
            }

            // 手机号格式校验
            if (!phoneNumber.matches("^1[3-9]\\d{9}$")) {
                return Result.error("400", "请输入正确的手机号格式");
            }

            // 密码强度校验
            if (password.length() < 8) {
                return Result.error("400", "密码长度不能少于8位");
            }

            // 检查密码是否包含至少两种字符类型（数字、小写字母、大写字母、特殊字符）
            boolean hasDigit = password.matches(".*\\d.*");
            boolean hasLower = password.matches(".*[a-z].*");
            boolean hasUpper = password.matches(".*[A-Z].*");
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

            int charTypeCount = 0;
            if (hasDigit) charTypeCount++;
            if (hasLower) charTypeCount++;
            if (hasUpper) charTypeCount++;
            if (hasSpecial) charTypeCount++;

            if (charTypeCount < 2) {
                return Result.error("400", "密码必须包含至少两种字符类型（数字、字母、特殊字符）");
            }

            // 检查用户名是否已存在
            if (userService.findByUsername(username) != null) {
                return Result.error("400", "用户名已存在");
            }

            // 检查邮箱是否已存在
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", email);
            if (userService.getOne(emailQuery) != null) {
                return Result.error("400", "邮箱已被注册");
            }

            // 检查手机号是否已存在
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("phone", phoneNumber);
            if (userService.getOne(phoneQuery) != null) {
                return Result.error("400", "手机号已被注册");
            }
            //前端传来的
            boolean isEncrypted = Boolean.parseBoolean(registerUser.getOrDefault("isEncrypted", "false"));

            // 2. 如果是加密传输，先解密
            if (forceRsaEncrypt || isEncrypted) {
               password = rsaUtil.decrypt(password);
            }

            // 3. 密码加密（明文 → BCrypt 密文）
            String encryptedPassword = passwordUtil.encrypt(password);

            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(encryptedPassword); // 实际项目中应该加密存储
            user.setEmail(email);
            user.setPhone(phoneNumber);
            user.setDepartmentId(departmentId); // 绑定部门ID（关键：与数据库department_id字段对应）
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setStatus(2); // 默认禁用,为2

            boolean saved = userService.save(user);
            if (!saved) {
                return Result.error("500", "注册失败，请稍后重试");
            }

            // 为新用户分配默认角色（假设默认角色ID为2）
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(2); // 默认角色ID
            userRole.setCreatedAt(LocalDateTime.now());
            userService.saveUserRole(userRole);

            // 获取新注册用户的完整信息
            Map<String, Object> userInfo = userService.getUserInfoWithRolesAndPermissions(user.getId());
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("token", "新注册用户请先登录");
            resultData.put("userInfo", userInfo);

            return Result.success(resultData);
        }
    @Log(title = "用户登录", businessType = BusinessType.GRANT)
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result login(@RequestBody Map<String, String> loginUser,
                        @RequestParam(required = false) String captchaKey,
                        @RequestParam(required = false) String captchaCode,
                        HttpServletRequest request) {
        try {
            // 验证码校验（使用Redis存储）
            if (captchaKey != null && captchaCode != null) {
                // 从Redis中获取验证码
                String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + captchaKey);

                // 校验验证码
                if (redisCaptcha == null || !redisCaptcha.equalsIgnoreCase(captchaCode)) {
                    return Result.error("400", "验证码错误");
                }

                // 验证成功后删除验证码
                redisTemplate.delete("captcha:" + captchaKey);
            }

            // 获取用户名和密码
            String username = loginUser.get("username");
            String password = loginUser.get("password");

            // 参数校验
            if (username == null || password == null) {
                return Result.error("400", "用户名或密码不能为空");
            }
//            System.out.println("用户登录密码为: "+password);
            boolean isEncrypted = Boolean.parseBoolean(loginUser.getOrDefault("isEncrypted", "false"));
            // 2. 如果是加密传输，先解密
            if (forceRsaEncrypt || isEncrypted) {
                password = rsaUtil.decrypt(password);
            }
            System.out.println("密码是否加密: "+isEncrypted);
            // 查询用户
            User user = userService.findByUsername(username);
            if (user == null) {
                return Result.error("400", "用户不存在");
            }
            // 新增：用户状态校验（仅status=1可登录）
            if (user.getStatus() != 1) {
                // 可根据实际业务调整提示语，如“账号已禁用”“账号未激活”等
                return Result.error("403", "账号未启用，无法登录");
            }

            // 4. 校验密码（自动兼容明文/密文）
            boolean passwordCorrect = passwordUtil.checkPassword(password, user.getPassword());
            if (!passwordCorrect) {
                return Result.error("400", "用户名或密码错误");
            }
            // 5. 自动升级：如果数据库是明文，改成密文（只执行一次）
            if (!passwordUtil.isEncrypted(user.getPassword())) {
                String encryptedPassword = passwordUtil.encrypt(password);
                user.setPassword(encryptedPassword);
                userService.updateById(user); // 更新数据库为密文
//                System.out.println("用户 " + username + " 密码已从明文升级为密文");
            }

            // 生成JWT token在用户登录成功后生成
            //使用用户密码作为签名密钥，确保token与用户绑定
            /*
            JWT token包含以下信息：
            Payload:
            aud (audience): 用户ID
            exp (expiration time): 过期时间（24小时后）
            Signature: 使用用户的密码作为密钥进行HMAC256签名
             */
            String token = JWT.create()
                    .withAudience(user.getId().toString())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24小时过期
                    .sign(Algorithm.HMAC256(user.getPassword()));
            // 获取用户完整信息（包含角色和权限
            Map<String, Object> userInfo = userService.getUserInfoWithRolesAndPermissions(user.getId());
            // 构造返回结果
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("token", token);
            resultData.put("userInfo", userInfo);

            return Result.success(resultData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "登录失败");
        }
    }

    /**
     * 获取用户详情信息（包含角色和权限）
     * @param id 用户ID
     * @return 用户详情信息
     */
    @RequireRole({"SUPER_ADMIN","USER_ADMIN"})
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情信息（包含角色和权限）")
    @GetMapping("/detail/{id}")
    public Result getUserDetail(@PathVariable Integer id) {
        try {
            Map<String, Object> userInfo = userService.getUserInfoWithRolesAndPermissions(id);
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.error("500", "获取用户详情失败");
        }
    }
    /**
     * 用户修改密码
     * @param passwordInfo 包含原密码、新密码
     * @param request 获取当前登录用户
     * @return 操作结果
     */
    @Log(title = "修改密码", businessType = BusinessType.UPDATE)
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改自身密码")
    public Result changePassword(@RequestBody Map<String, String> passwordInfo, HttpServletRequest request) {
        // 1. 前端参数非空/格式校验
        String oldPassword = passwordInfo.get("oldPassword");
        String newPassword = passwordInfo.get("newPassword");
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.error("400", "原密码不能为空");
        }
        // 2. Token合法性校验
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty() || !token.contains(".")) {
            return Result.error("401", "请携带有效Token登录后操作");
        }

        Integer userId = null;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            userId = Integer.parseInt(userIdStr);
        } catch (IndexOutOfBoundsException e) {
            return Result.error("401", "Token格式错误，无法解析用户信息");
        } catch (NumberFormatException e) {
            return Result.error("401", "Token中用户ID格式非法");
        } catch (Exception e) {
            return Result.error("401", "Token解析失败，请重新登录");
        }

        boolean isEncrypted = Boolean.parseBoolean(passwordInfo.getOrDefault("isEncrypted", "false"));

        // 2. 如果是加密传输，先解密
        if (forceRsaEncrypt || isEncrypted) {
            oldPassword = rsaUtil.decrypt(oldPassword);
            newPassword = rsaUtil.decrypt(newPassword);
        }
        // 4. 调用Service层修改密码
        try {
            boolean success = userService.changeUserPassword(userId, oldPassword, newPassword);
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("400", "原密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "系统异常，密码修改失败");
        }
    }

    /**
     * 管理员重置用户密码（仅管理员可操作）
     * @param id 用户ID
     * @param passwordInfo 重置密码信息（可传新密码，不传则使用默认密码）
     * @return 操作结果
     */
    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @RequireRole({"SUPER_ADMIN","USER_ADMIN"})
    @Operation(summary = "管理员重置用户密码", description = "根据用户ID重置密码，支持自定义密码或默认密码")
    @PostMapping("/reset-password/{id}")
    public Result resetUserPassword(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> passwordInfo) {
        try {
            // 1. 查询用户是否存在
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("404", "用户不存在");
            }

            // 2. 获取新密码（不传则使用默认密码 123456Ab）
            String newPassword = passwordInfo != null ? passwordInfo.get("newPassword") : null;
            if (!StringUtils.hasText(newPassword)) {
                newPassword = "123456Ab."; // 默认初始密码（满足密码强度要求：字母+数字，两种字符类型）
            } else {
                // 3. 校验新密码强度（与注册逻辑一致）
                if (newPassword.length() < 8) {
                    return Result.error("400", "密码长度不能少于8位");
                }
                boolean hasDigit = newPassword.matches(".*\\d.*");
                boolean hasLower = newPassword.matches(".*[a-z].*");
                boolean hasUpper = newPassword.matches(".*[A-Z].*");
                boolean hasSpecial = newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
                if (!hasDigit || !hasLower || !hasUpper || !hasSpecial) {
                    return Result.error("400", "密码必须同时包含数字、小写字母、大写字母和特殊字符");
                }
            }

            // 4. 密码加密（BCrypt）
            String encryptedPassword = passwordUtil.encrypt(newPassword);

            // 5. 更新用户密码
            user.setPassword(encryptedPassword);
            user.setUpdatedAt(LocalDateTime.now());
            boolean updateSuccess = userService.updateById(user);
            if (updateSuccess) {
                Map<String, String> resultData = new HashMap<>();
                resultData.put("defaultPassword", newPassword); // 返回重置后的密码（方便管理员告知用户）
                // 用原有方法，手动构建Result对象
                Result result = Result.success(resultData);
                result.setMessage("密码重置成功"); // 手动设置message
                return result;
            } else {
                return Result.error("500", "密码重置失败，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "系统异常，密码重置失败");
        }
    }

    @Operation(summary = "查询用户主管", description = "根据ID查询用户主管")
    @GetMapping("/findDepartment/{id}")
    public Result findDepartmentUser(@PathVariable Integer id) {
        return Result.success(userService.getDepartmentUser(id));
    }

    @Operation(summary = "查询审核人", description = "根据材料类别筛选审核人")
    @GetMapping("/getFirstApprover/{itemCategory}")
    public Result getFirstApprover(@PathVariable Integer itemCategory) {
        return Result.success(userService.getFirstApprover(itemCategory));
    }

    @GetMapping("/user-department/{userId}")
    @Operation(summary = "根据用户ID获取用户及部门信息")
    public Result getUserDepartmentInfo(@PathVariable Integer userId) {
        User userWithDept = userService.getUserWithDepartment(userId);
        if (userWithDept != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userWithDept.getId());
            result.put("username", userWithDept.getUsername());
            result.put("departmentId", userWithDept.getDepartmentId());
            result.put("departmentName", userWithDept.getDepartmentName());
            return Result.success(result);
        } else {
            return Result.error("404", "用户不存在");
        }
    }


}
