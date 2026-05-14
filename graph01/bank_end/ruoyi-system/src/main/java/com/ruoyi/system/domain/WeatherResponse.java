package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 天气响应根类（Lombok 简化版）
 */
@Data // 自动生成 Getter、Setter、toString、equals、hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {
    // 状态码
    private String status;
    // 结果数量
    private String count;
    // 状态信息
    private String info;
    // 信息码
    private String infocode;
    // 城市预报列表
    private List<Forecast> forecasts;
}