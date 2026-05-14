package com.ruoyi.system.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 高德地图逆地理编码API请求参数封装
 * 文档参考：https://lbs.amap.com/api/webservice/guide/api/georegeo
 */
@Data
@Accessors(chain = true) // 支持链式调用（如 request.setKey("xxx").setLocation("116.482086,39.990496")）
public class AmapRegeoRequest {

    /**
     * 高德Web服务API Key（必填）
     * 申请地址：https://console.amap.com/dev/key/app
     */
    private String key;

    /**
     * 经纬度坐标（必填）
     * 格式：经度在前，纬度在后，用英文逗号分隔，小数点后不超过6位
     * 示例：116.482086,39.990496
     */
    @Pattern(regexp = "^\\d{1,3}\\.\\d{1,6},\\d{1,3}\\.\\d{1,6}$", 
             message = "经纬度格式错误，正确格式：经度,纬度（小数点后不超过6位）")
    private String location;

    /**
     * 返回附近POI类型（可选）
     * 仅extensions=all时生效，支持多个TYPECODE用|分隔（参考POI分类码表）
     * 示例：050000|060000（餐饮服务|购物服务）
     */
    private String poitype;

    /**
     * 搜索半径（可选）
     * 范围：0~3000米，默认值：1000米
     */
    @Min(value = 0, message = "搜索半径不能小于0米")
    @Max(value = 3000, message = "搜索半径不能大于3000米")
    private Integer radius = 1000;

    /**
     * 返回结果控制（可选）
     * base：仅返回基本地址信息（默认）
     * all：返回地址+POI+道路+交叉口信息
     */
    private String extensions = "base";

    /**
     * 道路等级（可选）
     * 仅extensions=all时生效，0：显示所有道路，1：仅显示主干道路
     */
    private Integer roadlevel;

    /**
     * 数字签名（可选）
     * 参考：https://lbs.amap.com/faq/account/key/72
     */
    private String sig;

    /**
     * 返回数据格式（可选）
     * 支持JSON（默认）、XML
     */
    private String output = "JSON";

    /**
     * 回调函数（可选）
     * 仅output=JSON时有效，用于跨域请求
     */
    private String callback;

    /**
     * POI返回顺序优化（可选）
     * 仅extensions=all时生效，0：默认排序，1：优先居家POI，2：优先公司POI
     */
    @Min(value = 0, message = "homeorcorp只能是0、1、2")
    @Max(value = 2, message = "homeorcorp只能是0、1、2")
    private Integer homeorcorp = 0;

    // 额外校验：确保roadlevel、poitype、homeorcorp仅在extensions=all时生效（可通过业务代码补充）
    /**
     * 校验参数合法性（补充框架未覆盖的业务规则）
     * @throws IllegalArgumentException 校验失败抛出异常
     */
    public void validate() {
        String ext = this.getExtensions();
        if (!"all".equals(ext)) {
            if (this.getRoadlevel() != null) {
                throw new IllegalArgumentException("roadlevel参数仅在extensions=all时生效");
            }
            if (this.getPoitype() != null && !this.getPoitype().isEmpty()) {
                throw new IllegalArgumentException("poitype参数仅在extensions=all时生效");
            }
            if (this.getHomeorcorp() != 0) {
                throw new IllegalArgumentException("homeorcorp参数仅在extensions=all时生效");
            }
        }
        if ("XML".equals(this.getOutput()) && this.getCallback() != null && !this.getCallback().isEmpty()) {
            throw new IllegalArgumentException("callback参数仅在output=JSON时生效");
        }
    }
}