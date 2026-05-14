package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逆地理编码响应结果
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GaoDeRegeocodeResponse {
    /**
     * 返回结果状态值：0-失败，1-成功
     */
    private int status;

    /**
     * 返回状态说明：失败时为错误原因，成功时为"OK"
     */
    private String info;

    /**
     * 逆地理编码列表
     */
    private Regeocode regeocode;
}
