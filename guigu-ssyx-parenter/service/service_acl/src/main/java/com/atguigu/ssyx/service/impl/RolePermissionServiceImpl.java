package com.atguigu.ssyx.service.impl;

import com.atguigu.ssyx.mapper.RolePermissionMapper;
import com.atguigu.ssyx.model.acl.Permission;
import com.atguigu.ssyx.model.acl.RolePermission;
import com.atguigu.ssyx.service.PermissionService;
import com.atguigu.ssyx.service.RolePermissionService;
import com.atguigu.ssyx.utils.PermissionHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: RolePermissionServiceImpl
 * Package: com.atguigu.ssyx.service.impl
 * Description:
 *

 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper,RolePermission> implements RolePermissionService {
    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private PermissionService permissionService;

    @Override
    public List<Permission> selectAllList(Long roleId) {
        //1.得到了permission里面的所有项
        List<Permission> allPermissionList = permissionService.list();

        //2.得到了对应roleId的所有permissionId所有项
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> RolePermissions = rolePermissionService.list(queryWrapper);
        List<Long> permissionIds = new ArrayList<>();
        for (RolePermission rolePermission : RolePermissions) {
            permissionIds.add(rolePermission.getPermissionId());
        }

        //3.遍历permission，如果有对应的被从关系表里选出来的permission一致的话，那么就可以得到内容了
        for (Permission permission : allPermissionList) {
            if (permissionIds.contains(permission.getId())) {
                permission.setSelect(true);
            }
        }

        //4.借用工具类改成树形结构返回
        List<Permission> result = PermissionHelper.buildPermission(allPermissionList);

        return result;
    }

    @Override
    public void addAllList(Long roleId, Long[] permissionIds) {
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionService.remove(queryWrapper);

        List<RolePermission> rolePermissions = new ArrayList<>();

        for (Long permissionId : permissionIds) {
            rolePermissions.add(new RolePermission(roleId, permissionId));
        }
        //rolePermissionService.save(new RolePermission(roleId,permissionId));
        rolePermissionService.saveBatch(rolePermissions);

    }


}
