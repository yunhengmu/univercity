package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每日天气预报类（Lombok 简化版）
 */
@Data // 自动生成 Getter、Setter、toString、equals、hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cast {
    // 日期
    private String date;
    // 星期
    private String week;
    // 白天天气
    private String dayweather;
    // 夜间天气
    private String nightweather;
    // 白天温度（字符串型）
    private String daytemp;
    // 夜间温度（字符串型）
    private String nighttemp;
    // 白天风向
    private String daywind;
    // 夜间风向
    private String nightwind;
    // 白天风力
    private String daypower;
    // 夜间风力
    private String nightpower;
    // 白天温度（浮点型，JSON 下划线字段映射）
    @JsonProperty("daytemp_float")
    private Double daytempFloat;
    // 夜间温度（浮点型，JSON 下划线字段映射）
    @JsonProperty("nighttemp_float")
    private Double nighttempFloat;
}