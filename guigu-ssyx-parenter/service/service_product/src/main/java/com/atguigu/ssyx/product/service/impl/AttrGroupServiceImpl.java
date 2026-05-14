package com.atguigu.ssyx.product.service.impl;

import com.atguigu.ssyx.model.product.AttrGroup;
import com.atguigu.ssyx.product.mapper.AttrGroupMapper;
import com.atguigu.ssyx.product.service.AttrGroupService;
import com.atguigu.ssyx.vo.product.AttrGroupQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ClassName: AttrGroupServiceImpl
 * Package: com.atguigu.ssyx.product.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/22 14:25
 * @Version 1.0
 */
@Service
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper,AttrGroup> implements AttrGroupService {
    @Override
    public IPage<AttrGroup> getAttrGroupList(Page attrPage, AttrGroupQueryVo attrGroupQueryVo) {
        String name = attrGroupQueryVo.getName();
        LambdaQueryWrapper<AttrGroup> queryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like(AttrGroup::getName,name);
        }
        IPage<AttrGroup> iPage = baseMapper.selectPage(attrPage,queryWrapper);
        return iPage;
    }
}
