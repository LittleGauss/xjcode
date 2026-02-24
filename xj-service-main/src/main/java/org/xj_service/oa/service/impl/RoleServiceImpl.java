package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.Role;
import org.xj_service.oa.entity.RolePermission;
import org.xj_service.oa.entity.Permission;
import org.xj_service.oa.mapper.RoleMapper;
import org.xj_service.oa.mapper.RolePermissionMapper;
import org.xj_service.oa.mapper.PermissionMapper;
import org.xj_service.oa.service.IRoleService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {
    @javax.annotation.Resource
    private RolePermissionMapper rolePermissionMapper;

    @javax.annotation.Resource
    private PermissionMapper permissionMapper;

    @Override
    public void exportRolesToExcel(OutputStream outputStream) throws IOException {
        try {
            List<Role> roleList = list();

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("角色列表");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "角色编码", "角色名称", "角色描述", "创建时间", "更新时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            for (int i = 0; i < roleList.size(); i++) {
                Role role = roleList.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(role.getId());
                row.createCell(1).setCellValue(role.getCode());
                row.createCell(2).setCellValue(role.getName());
                row.createCell(3).setCellValue(role.getDescription());
                row.createCell(4).setCellValue(role.getCreatedAt() != null ?
                        role.getCreatedAt().toString() : "");
                row.createCell(5).setCellValue(role.getUpdatedAt() != null ?
                        role.getUpdatedAt().toString() : "");
            }

            // 写入输出流
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (Exception e) {
            throw new IOException("导出角色数据失败", e);
        }
    }

    @Override
    public List<Role> listWithPermissions() {
        List<Role> roles = list();
        if (roles.isEmpty()) {
            return roles;
        }
        // 所有角色ID
        List<Integer> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        // 查询角色-权限关联
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RolePermission> rpQw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        rpQw.in("role_id", roleIds);
        List<RolePermission> rolePerms = rolePermissionMapper.selectList(rpQw);
        if (rolePerms.isEmpty()) {
            return roles; // 没有关联则直接返回（前端会显示“无”）
        }
        // 根据关联查询权限明细
        List<Integer> permIds = rolePerms.stream().map(RolePermission::getPermissionId).distinct().collect(Collectors.toList());
        if (permIds.isEmpty()) {
            return roles;
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Permission> pQw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        pQw.in("id", permIds);
        List<Permission> permissions = permissionMapper.selectList(pQw);
        // 建立权限ID到实体的映射
        java.util.Map<Integer, Permission> permMap = permissions.stream().collect(java.util.stream.Collectors.toMap(Permission::getId, p -> p));
        // 给每个角色填充权限列表
        java.util.Map<Integer, List<Permission>> roleToPerms = rolePerms.stream().collect(java.util.stream.Collectors.groupingBy(RolePermission::getRoleId, java.util.stream.Collectors.mapping(rp -> permMap.get(rp.getPermissionId()), java.util.stream.Collectors.toList())));
        roles.forEach(r -> r.setPermissions(roleToPerms.getOrDefault(r.getId(), java.util.Collections.emptyList())));
        return roles;
    }
}
