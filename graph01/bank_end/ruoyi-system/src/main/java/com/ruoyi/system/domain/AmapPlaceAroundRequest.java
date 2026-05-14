package com.ruoyi.system.domain;

import com.ruoyi.system.domain.ExtensionsEnum;
import com.ruoyi.system.domain.SortRuleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.regex.Pattern;

/**
 * 高德地图 v3/place/around 周边POI查询请求参数类
 * 严格遵循高德API参数规范：https://lbs.amap.com/api/webservice/guide/api/placearound
 */
@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmapPlaceAroundRequest {
    // ===================== 必选参数 =====================
    /**
     * 服务权限标识（必填）
     * 说明：用户在高德地图官网申请的Web服务API类型KEY
     */
    private String key;

    /**
     * 中心点坐标（必填）
     * 规则：经度和纬度用","分割，经度在前，纬度在后，经纬度小数点后不得超过6位
     * 示例："116.473168,39.993015"
     */
    private String location;

    // ===================== 可选参数 =====================
    /**
     * 查询关键字（可选）
     * 规则：只支持一个关键字
     */
    private String keywords;

    /**
     * 查询POI类型（可选）
     * 规则：多个类型用“|”分割；支持分类代码（6位数字）或汉字（需严格匹配官方分类）
     * 默认值：当keywords和types均为空时，默认值为"050000|070000|120000"（餐饮服务|生活服务|商务住宅）
     */
    private String types;

    /**
     * 查询城市（可选）
     * 可选值：城市中文、中文全拼、citycode、adcode（如：北京/beijing/010/110000）
     * 默认值：无（全国范围内搜索）
     */
    private String city;

    /**
     * 查询半径（可选）
     * 取值范围：0-50000米；大于50000按默认值处理
     * 默认值：5000
     */
    private Integer radius = 5000;

    /**
     * 排序规则（可选）
     * 可选值：distance（按距离排序）、weight（综合排序）
     * 默认值：distance
     */
    private SortRuleEnum sortrule = SortRuleEnum.DISTANCE;

    /**
     * 每页记录数（可选）
     * 规则：强烈建议不超过25，超过可能导致访问报错
     * 默认值：20
     */
    private Integer offset = 20;

    /**
     * 当前页数（可选）
     * 默认值：1
     */
    private Integer page = 1;

    /**
     * 返回结果控制（可选）
     * 可选值：base（仅基础地址）、all（完整信息）
     * 默认值：base
     */
    private ExtensionsEnum extensions = ExtensionsEnum.BASE;

    /**
     * 数字签名（可选）
     * 说明：数字签名获取和使用方法参考高德官方文档
     */
    private String sig;

    /**
     * 回调函数（可选）
     * 说明：仅当output=JSON时有效，值为用户定义的函数名称
     */
    private String callback;

    // ===================== 参数校验方法 =====================
    /**
     * 校验必填参数和参数合法性
     * @return 校验通过返回true，否则返回false
     */
    public boolean validate() {
        // 1. 校验必填参数
        if (key == null || key.trim().isEmpty()) {
            log.error("请求参数校验失败：key为必填项");
            return false;
        }
        if (location == null || location.trim().isEmpty()) {
            log.error("请求参数校验失败：location为必填项");
            return false;
        }

        // 2. 校验location格式（经度,纬度，小数点后不超过6位）
        String locationPattern = "^\\d{1,3}\\.\\d{1,6},\\d{1,3}\\.\\d{1,6}$";
        if (!Pattern.matches(locationPattern, location.trim())) {
            log.error("请求参数校验失败：location格式错误，需满足「经度,纬度」且小数点后不超过6位，当前值：{}", location);
            return false;
        }

        // 3. 校验radius范围
        if (radius != null && (radius < 0 || radius > 50000)) {
            log.warn("radius超出0-50000范围，自动重置为默认值5000，当前值：{}", radius);
            this.radius = 5000;
        }

        // 4. 校验offset上限
        if (offset != null && offset > 25) {
            log.warn("offset超过25，自动重置为25，当前值：{}", offset);
            this.offset = 25;
        }

        // 5. 处理types默认值（当keywords和types均为空时）
        if ((keywords == null || keywords.trim().isEmpty()) && (types == null || types.trim().isEmpty())) {
            this.types = "050000|070000|120000";
            log.info("keywords和types均为空，自动设置types默认值：{}", this.types);
        }

        return true;
    }

    // ===================== 辅助方法：转换为URL参数格式 =====================
    /**
     * 将请求参数转换为URL拼接的参数字符串（已处理空值和枚举转换）
     * @return URL参数字符串（格式：key1=value1&key2=value2）
     */
    public String toUrlParams() {
        if (!validate()) {
            throw new IllegalArgumentException("请求参数校验失败，无法生成URL参数");
        }

        StringBuilder params = new StringBuilder();
        // 必选参数
        params.append("key=").append(key.trim());
        params.append("&location=").append(location.trim());

        // 可选参数（非空才拼接）
        if (keywords != null && !keywords.trim().isEmpty()) {
            params.append("&keywords=").append(keywords.trim());
        }
        if (types != null && !types.trim().isEmpty()) {
            params.append("&types=").append(types.trim());
        }
        if (city != null && !city.trim().isEmpty()) {
            params.append("&city=").append(city.trim());
        }
        params.append("&radius=").append(radius);
        params.append("&sortrule=").append(sortrule.getValue());
        params.append("&offset=").append(offset);
        params.append("&page=").append(page);
        params.append("&extensions=").append(extensions.getValue());
        if (sig != null && !sig.trim().isEmpty()) {
            params.append("&sig=").append(sig.trim());
        }
        if (callback != null && !callback.trim().isEmpty()) {
            params.append("&callback=").append(callback.trim());
        }

        return params.toString();
    }
}