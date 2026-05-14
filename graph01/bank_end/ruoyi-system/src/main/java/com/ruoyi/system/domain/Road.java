package com.ruoyi.system.domain;

import lombok.Data;
import java.io.Serializable;

/**
 * 道路信息
 */
@Data
public class Road implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 道路ID */
    private String id;
    /** 道路名称 */
    private String name;
    /** 方向 */
    private String direction;
    /** 距离（米） */
    private String distance;
    /** 经纬度（格式：经度,纬度） */
    private String location;
}