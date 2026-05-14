package com.atguigu.ssyx.service;

import com.atguigu.ssyx.model.acl.Permission;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: PermissionService
 * Package: com.atguigu.ssyx.service
 * Description:
 *

 */
@Service
public interface PermissionService extends IService<Permission> {
    List<Permission> queryAllList();

    void removeChildById(Long id);


}
