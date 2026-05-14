package com.atguigu.ssyx.service.impl;

import com.atguigu.ssyx.mapper.PermissionMapper;
import com.atguigu.ssyx.model.acl.Permission;
import com.atguigu.ssyx.model.acl.RolePermission;
import com.atguigu.ssyx.service.PermissionService;
import com.atguigu.ssyx.utils.PermissionHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: PermissionServiceImpl
 * Package:
 * com.atguigu.ssyx.service.impl
 * Description:
 *
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    @Autowired
    private PermissionService permissionService;

    @Override
    public List<Permission> queryAllList() {
        //1. 查询所有菜单
        List<Permission> allPermissionList =
                baseMapper.selectList(null);

        //2. 转换要求数据格式
        List<Permission> result = PermissionHelper.buildPermission(allPermissionList);

        return result;
    }

    @Override
    public void removeChildById(Long id) {
        List<Long> ids = new ArrayList<>();
        // 先添加当前节点ID
        ids.add(id);
        // 递归获取所有子节点ID
        findChildIds(id, ids);
        // 批量删除（MyBatis-Plus）
        permissionService.removeByIds(ids);
    }

    // 递归查找所有子节点ID
    private void findChildIds(Long parentId, List<Long> ids) {
        // 根据父ID查询直接子节点
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getPid, parentId); // 关键：按父ID查询

        List<Permission> children = permissionService.list(queryWrapper);

        for (Permission child : children) {
            Long childId = child.getId();
            ids.add(childId); // 添加子节点ID
            findChildIds(childId, ids); // 递归查找孙子节点
        }
    }


}
