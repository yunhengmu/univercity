package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 城市级预报类（Lombok 简化版）
 */
@Data // 自动生成 Getter、Setter、toString、equals、hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forecast {
    // 城市名称
    private String city;
    // 行政区划代码
    private String adcode;
    // 省份
    private String province;
    // 预报发布时间
    private String reporttime;
    // 每日预报列表
    private List<Cast> casts;
}