package com.atguigu.ssyx.service;

import com.atguigu.ssyx.model.acl.Permission;
import com.atguigu.ssyx.model.acl.RolePermission;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: RolePermissionService
 * Package: com.atguigu.ssyx.service
 * Description:
 *

 */
@Service
public interface RolePermissionService extends IService<RolePermission> {
    List<Permission> selectAllList(Long roleId);

    void addAllList(Long roleId, Long[] permissionId);
}
