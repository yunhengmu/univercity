package com.ruoyi.system.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * POI（兴趣点）信息
 */
@Data
public class Poi implements Serializable {
    private static final long serialVersionUID = 1L;

    /** POI唯一ID */
    private String id;
    /** 名称 */
    private String name;
    /** 类型（如：商务住宅;楼宇;商务写字楼） */
    private String type;
    /** 联系电话（数组） */
    // 核心修复：添加自定义反序列化器
    @JsonDeserialize(using = TelListDeserializer.class)
    private List<String> tel;
    /** 方向 */
    private String direction;
    /** 距离（米） */
    private String distance;
    /** 经纬度（格式：经度,纬度） */
    private String location;
    /** 详细地址 */
    private String address;
    /** POI权重 */
    private String poiweight;
    /** 所属商圈 */
    @JsonDeserialize(using = FlexibleStringDeserializer.class)
    private String businessarea;
}