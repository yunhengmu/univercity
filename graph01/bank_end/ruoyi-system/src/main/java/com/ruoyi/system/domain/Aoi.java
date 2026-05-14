package com.ruoyi.system.domain;

import lombok.Data;
import java.io.Serializable;

/**
 * AOIs（兴趣面）信息
 */
@Data
public class Aoi implements Serializable {
    private static final long serialVersionUID = 1L;

    /** AOI唯一ID */
    private String id;
    /** 名称 */
    private String name;
    /** 行政区划编码 */
    private String adcode;
    /** 经纬度（格式：经度,纬度） */
    private String location;
    /** 面积（平方米） */
    private String area;
    /** 距离（米） */
    private String distance;
    /** 类型编码 */
    private String type;
}