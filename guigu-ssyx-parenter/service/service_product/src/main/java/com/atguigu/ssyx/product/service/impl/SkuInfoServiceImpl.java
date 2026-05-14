package com.atguigu.ssyx.product.service.impl;

import com.atguigu.ssyx.model.product.SkuAttrValue;
import com.atguigu.ssyx.model.product.SkuImage;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.product.SkuPoster;
import com.atguigu.ssyx.mq.constant.MqConst;
import com.atguigu.ssyx.mq.service.RabbitService;
import com.atguigu.ssyx.product.mapper.SkuInfoMapper;
import com.atguigu.ssyx.product.service.SkuAttrValueService;
import com.atguigu.ssyx.product.service.SkuImageService;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.product.service.SkuPosterService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * ClassName: AttrGroupServiceImpl
 * Package: com.atguigu.ssyx.product.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/22 14:25
 * @Version 1.0
 */
@Transactional(rollbackFor = {Exception.class})
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImageService skuImageService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private SkuPosterService  skuPosterService;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public IPage<SkuInfo> getListSkuInfo(Page pageItem, SkuInfoQueryVo skuInfoQueryVo) {
        Long categoryId = skuInfoQueryVo.getCategoryId();
        String skuType = skuInfoQueryVo.getSkuType();
        String keyword = skuInfoQueryVo.getKeyword();
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(categoryId)){
            queryWrapper.eq(SkuInfo::getCategoryId,categoryId);
        }
        if(!StringUtils.isEmpty(skuType)){
            queryWrapper.like(SkuInfo::getSkuType,skuType);
        }
        if(!StringUtils.isEmpty(keyword)){
            queryWrapper.like(SkuInfo::getSkuName,keyword);
        }
        IPage page = baseMapper.selectPage(pageItem, queryWrapper);
        return page;
    }

    @Override
    public void saveMany(SkuInfoVo skuInfoVo) {
        //1. 添加sku的基本信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        baseMapper.insert(skuInfo);

        //2.保存sku的海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)){
            for(SkuPoster skuPoster:skuPosterList){
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //3.保存sku图片
        List<SkuImage> skuImages =  skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImages)){
            for(SkuImage skuImage:skuImages){
                skuImage.setSkuId(skuInfo.getId());
            }
            skuImageService.saveBatch(skuImages);
        }

        //4.保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            for(SkuAttrValue skuAttrValue:skuAttrValueList){
                skuAttrValue.setSkuId(skuInfo.getId());
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }

    }

    @Override
    public void updateSku(SkuInfoVo skuInfoVo) {
        Long id = skuInfoVo.getId();
        SkuInfo skuInfo = new SkuInfo();
        baseMapper.updateById(skuInfoVo);
        //1.海报
        //删除
        skuPosterService.remove(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId,id));
        //再保存
        List<SkuPoster> skuPosters =  skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosters)){
            for(SkuPoster skuPoster:skuPosters){
                skuPoster.setSkuId(skuInfo.getId());
            }
        }
        skuPosterService.saveBatch(skuPosters);
        //2.图片
        //删除
        skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,id));
        List<SkuImage> skuImages =  skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImages)){
            for(SkuImage skuImage:skuImages){
                skuImage.setSkuId(skuInfo.getId());
            }
        }
        skuImageService.saveBatch(skuImages);
        //3.属性
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId,id));
        List<SkuAttrValue> skuAttrValueList =  skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            for(SkuAttrValue skuAttrValue:skuAttrValueList){
                skuAttrValue.setSkuId(skuInfo.getId());
            }
        }
        skuAttrValueService.saveBatch(skuAttrValueList);
    }

    @Override
    public SkuInfoVo getItemById(Long id) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();
        SkuInfo skuInfo = baseMapper.selectById(id);
        List<SkuPoster>  skuPosterList = skuPosterService.list(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId,id));
        List<SkuImage>  skuImages = skuImageService.list(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,id));
        List<SkuAttrValue> skuAttrValues = skuAttrValueService.list(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId,id));

        BeanUtils.copyProperties(skuInfo,skuInfoVo);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuImagesList(skuImages);
        skuInfoVo.setSkuAttrValueList(skuAttrValues);
        return skuInfoVo;
    }

    @Override
    public void itemUp(Long id, Integer status) {
        SkuInfo skuInfo = baseMapper.selectById(id);
        skuInfo.setId(id);
        skuInfo.setCheckStatus(status);
        baseMapper.updateById(skuInfo);
    }

    @Override
    public void check(Long id, Integer status) {
        if(status == 1) {
            SkuInfo skuInfoUp = new SkuInfo();
            skuInfoUp.setId(id);
            skuInfoUp.setPublishStatus(1);
            baseMapper.updateById(skuInfoUp);
            //TODO 商品上架 后续会完善：发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,MqConst.ROUTING_GOODS_UPPER,id);
        } else {
            SkuInfo skuInfoUp = new SkuInfo();
            skuInfoUp.setId(id);
            skuInfoUp.setPublishStatus(0);
            baseMapper.updateById(skuInfoUp);
            //TODO 商品下架 后续会完善：发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_LOWER, id);
        }
    }

    @Override
    public void isNewPerson(Long id, Integer status) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(id);
        skuInfo.setIsNewPerson(status);
        baseMapper.updateById(skuInfo);
    }
}
