package com.atguigu.ssyx.product.service;

import com.atguigu.ssyx.model.product.AttrGroup;
import com.atguigu.ssyx.vo.product.AttrGroupQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * ClassName: AttrGroupService
 * Package: com.atguigu.ssyx.product.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/22 14:21
 * @Version 1.0
 */
public interface AttrGroupService extends IService<AttrGroup> {
    IPage<AttrGroup> getAttrGroupList(Page attrPage, AttrGroupQueryVo attrGroupQueryVo);
}
