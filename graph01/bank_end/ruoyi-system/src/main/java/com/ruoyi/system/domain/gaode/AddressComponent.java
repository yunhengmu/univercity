package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 地址元素
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressComponent {
    /**
     * 国家
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String country;

    /**
     * 省份
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String province;

    /**
     * 城市
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String city;

    /**
     * 城市编码
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String citycode;

    /**
     * 区
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String district;

    /**
     * 行政区编码
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String adcode;

    /**
     * 乡镇/街道
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String township;

    /**
     * 乡镇街道编码
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String towncode;

    /**
     * 社区信息
     */
    private Neighborhood neighborhood;

    /**
     * 楼信息
     */
    private Building building;

    /**
     * 门牌信息
     */
    private StreetNumber streetNumber;

    /**
     * 所属海域信息
     */
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String seaArea;

    /**
     * 经纬度所属商圈列表
     */
    @JsonDeserialize(using = EmptyArrayAsNullListDeserializer.class)
    private List<BusinessArea> businessAreas;
}
