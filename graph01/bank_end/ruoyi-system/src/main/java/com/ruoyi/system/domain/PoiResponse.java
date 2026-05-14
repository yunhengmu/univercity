package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 充电站POI响应根对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoiResponse {
    /** 建议信息 */
    private Suggestion suggestion;
    /** 总数 */
    private String count;
    /** 状态码 */
    private String infocode;
    /** POI列表 */
    private List<PoiItem> pois;
    /** 状态（1=成功） */
    private String status;
    /** 响应信息 */
    private String info;
}