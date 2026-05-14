package com.atguigu.ssyx.service.impl;

import com.atguigu.ssyx.mapper.AdminRoleMapper;
import com.atguigu.ssyx.model.acl.AdminRole;
import com.atguigu.ssyx.service.AdminRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * ClassName: AdminRoleServiceImpl
 * Package: com.atguigu.ssyx.service.impl
 * Description:
 *

 */
@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper,AdminRole> implements AdminRoleService {
}
