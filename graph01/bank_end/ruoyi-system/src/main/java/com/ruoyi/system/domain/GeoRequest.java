package com.ruoyi.system.domain;
import com.ruoyi.common.config.AmapConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoRequest {
    private String address;
    private String key;

    // 构造方法
    public GeoRequest(String address) {
        this.address = address;
        this.key = AmapConfig.KEY;
    }
}