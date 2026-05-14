package com.atguigu.ssyx.product.service;

import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
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
public interface SkuInfoService extends IService<SkuInfo> {
    IPage<SkuInfo> getListSkuInfo(Page pageItem, SkuInfoQueryVo skuInfoQueryVo);

    void saveMany(SkuInfoVo skuInfoVo);

    void updateSku(SkuInfoVo skuInfoVo);

    SkuInfoVo getItemById(Long id);

    void itemUp(Long id, Integer status);

    void check(Long id, Integer status);

    void isNewPerson(Long id, Integer status);
}
