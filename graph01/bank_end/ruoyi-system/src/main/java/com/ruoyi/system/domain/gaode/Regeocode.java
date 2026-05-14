package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逆地理编码详细信息
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Regeocode {
    /**
     * 地址元素列表
     */
    private AddressComponent addressComponent;
    /**
     * 地址信息
     */
    @JsonProperty("formatted_address")
    private String formattedAddress;
}
