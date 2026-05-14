package com.atguigu.ssyx.service;

import com.atguigu.ssyx.model.acl.Admin;
import com.atguigu.ssyx.vo.acl.AdminQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

/**
 * ClassName: AdminService
 * Package: com.atguigu.ssyx.service
 * Description:
 *

 */
@Service
public interface AdminService extends IService<Admin> {
    IPage<Admin>  selectPageUser(Page<Admin> pageParam, AdminQueryVo roleQueryVo);
}
