package com.atguigu.ssyx.service.impl;


import com.atguigu.ssyx.mapper.RoleMapper;
import com.atguigu.ssyx.model.acl.AdminRole;
import com.atguigu.ssyx.service.AdminRoleService;
import com.atguigu.ssyx.service.RoleService;

import com.atguigu.ssyx.model.acl.Role;
import com.atguigu.ssyx.vo.acl.RoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: RoleServiceImpl
 * Package: com.atguigu.ssyx.service.impl
 * Description:
 *

 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    //用户角色关系
    @Autowired
    private AdminRoleService adminRoleService;


    @Override
    public IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        String roleName = roleQueryVo.getRoleName();

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        if(!StringUtils.isEmpty(roleName)){ //判断空和空字符串
            wrapper.like(Role::getRoleName,roleName);//相当于rolename like？
        }
        IPage<Role> roleIPage = baseMapper.selectPage(pageParam,wrapper);

        return roleIPage;
    }



    @Override
    public Map<String, Object> getRoleByAdminId(Long adminId) {
        //1 查询所有角色
        List<Role> allRoleList = baseMapper.selectList(null);
        allRoleList.forEach(role -> {
            System.out.println(role.getRoleName()+","+role.getId());
        });

        //2 根据用户id查询用户分配角色列表
        //2.1 根据用户id查询用户角色关系表，得出用户已经分配的角色id的列表
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        //设置查询条件，根据id adminId
        wrapper.eq(AdminRole::getAdminId,adminId);

        List<AdminRole> adminRoleList = adminRoleService.list(wrapper);

        adminRoleList.forEach(adminRole -> {
            System.out.println(adminRole.getAdminId()+","+adminRole.getRoleId()+","+adminRole.getId());
        });

        //2.2 通过第一步返回的集合，获取的所有角色id的列表
         List<Long> roleIdsList =
                 adminRoleList.stream()
                .map(item->item.getRoleId())
                .collect(Collectors.toList());

        roleIdsList.forEach(roleId -> {
            System.out.println(roleId);
        });
        //2.3 创建新的list集合，用于存储用户配置角色
        List<Role> assignRoleList = new ArrayList<>();

        //2.4 遍历所有角色列表 allRoleslist，得到每个角色
        for (Role role : allRoleList) {
            //判断
            if(roleIdsList.contains(role.getId())){
                assignRoleList.add(role);
            }
        }
        //判断所有角色里面是否包含已经分配角色id，有封装到新的list集合中去
        Map<String,Object> result = new HashMap<>();
        //用户分配角色列表
        result.put("assignRoles",assignRoleList);
        //所有角色
        result.put("allRolesList",allRoleList);
        return result;
    }




    //为用户进行分配
    @Override
    public void saveAdminRole(Long adminId, Long[] roleIds) {
        //1 删除用户已经分配过的角色数据
        //根据用户id删除admin_role表里面对应数据
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        System.out.println("wrapper = " + wrapper);
        adminRoleService.remove(wrapper);

        //2 重新分配
        //adminId 1   roleId: 2 3
        //遍历多个角色id，得到每个角色id，拿着每个角色id + 用户id添加用户角色关系表
      /*  for (Long roleId : roleIds) {
            adminRoleService.save(new AdminRole(adminId,roleId));
        }*/
        List<AdminRole> adminRoleList = new ArrayList<>();
        for (Long roleId : roleIds) {//我们要少访问sql
            adminRoleList.add(new AdminRole(roleId,adminId));
        }
//        adminRoleList.forEach(adminRole -> {
//            System.out.println(adminRole.getAdminId()+","+adminRole.getRoleId());
//        });//在开始使用了sout之后就可以正常使用，之前一直把关系表按相反的方向放置
        adminRoleService.saveBatch(adminRoleList);
    }
}
