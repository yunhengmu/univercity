package com.atguigu.ssyx.product.mapper;

import com.atguigu.ssyx.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ClassName: CategoryMapper
 * Package: com.atguigu.ssyx.product.mapper
 * Description:

 */
public interface SkuInfoMapper extends Mapper<SkuInfo>, BaseMapper<SkuInfo> {
    SkuInfo checkStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    Integer lockStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);

    Integer unlockStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);
}
