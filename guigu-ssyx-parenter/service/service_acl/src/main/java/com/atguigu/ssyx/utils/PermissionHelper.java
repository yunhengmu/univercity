package com.atguigu.ssyx.utils;

import com.atguigu.ssyx.mapper.PermissionMapper;
import com.atguigu.ssyx.model.acl.Permission;
import com.atguigu.ssyx.model.acl.RolePermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atguigu.ssyx.mapper.PermissionMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: PermissionHelperImpl
 * Package: com.atguigu.ssyx.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/15 15:04
 * @Version 1.0
 */

public  class  PermissionHelper  {


    public static List<Permission> buildPermission(List<Permission> allList) {
        List<Permission> resultList = new ArrayList<>();
        for (Permission permission : allList) {
            if(permission.getPid()==0){
                permission.setLevel(1);
                resultList.add(findChildList(permission,allList));
            }
        }
        return  resultList;
    }

    public static Permission findChildList(Permission permission, List<Permission> allList) {
        permission.setChildren(new ArrayList<>());
        for (Permission childPermission : allList) {
            if (childPermission.getPid().longValue() == permission.getId().longValue()) {
                int i = permission.getLevel()+ 1;
                childPermission.setLevel(i);
                permission.getChildren().add(findChildList(childPermission,allList));
            }
        }
        return permission;
    }

}
