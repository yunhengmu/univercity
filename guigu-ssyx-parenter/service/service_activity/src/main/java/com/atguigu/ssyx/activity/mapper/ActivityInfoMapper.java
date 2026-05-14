package com.atguigu.ssyx.activity.mapper;

import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.activity.ActivityRule;
import com.atguigu.ssyx.model.activity.ActivitySku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ClassName: ActivityInfoMapper
 * Package: com.atguigu.ssyx.activity.mapper
 * Description:
 *
 */
@Repository
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {

    List<Long> selectExistSkuIdList(@Param("skuIdList")List<Long> skuIdList);

    List<ActivityRule> selectActivityRuleList(@Param("skuId")Long skuId);
    //根据skuId得到对应的规则列表
    List<ActivityRule> findActivity(@Param("skuId") Long skuId);

    List<ActivitySku> selectCartActivity(@Param("skuIdList") List<Long> skuIdList);
}
