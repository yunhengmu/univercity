package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: ReverseGeoRequest
 * Package: com.ruoyi.system.domain
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/12/21 18:37
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReverseGeoRequest {
    /**
     * 高德Web服务API Key（必填）
     */
    private String key;

    /**
     * 经纬度坐标（必填）
     * 规则：经度在前，纬度在后，以英文逗号分隔，小数点后不超过6位
     * 示例：116.481028,39.921983
     */
    private String location;

    /**
     * 返回附近POI类型（可选）
     * 仅extensions=all时生效，支持多个TYPECODE用英文竖线分隔
     */
    private String poitype;

    /**
     * 搜索半径（可选）
     * 取值范围：0~3000，默认值：1000，单位：米
     */
    private Integer radius = 1000;

    /**
     * 返回结果控制（可选）
     * 默认值：base（仅返回基本地址信息）；all（返回完整信息：地址+POI+道路+交叉口）
     */
    private String extensions = "base";

    /**
     * 道路等级（可选）
     * 仅extensions=all时生效，0：显示所有道路；1：仅显示主干道路
     */
    private Integer roadlevel;

    /**
     * 数字签名（可选）
     * 参考高德官方数字签名生成规则
     */
    private String sig;

    /**
     * 返回数据格式（可选）
     * 默认值：JSON；可选值：JSON/XML
     */
    private String output = "JSON";

    /**
     * 回调函数名（可选）
     * 仅output=JSON时生效
     */
    private String callback;

    /**
     * POI返回顺序优化（可选）
     * 仅extensions=all时生效，默认值：0
     * 0：不优化；1：优先返回居家相关POI；2：优先返回公司相关POI
     */
    private Integer homeorcorp = 0;
}
