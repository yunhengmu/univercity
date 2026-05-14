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
        // 注意：key 应该在调用方设置，从配置中获取
        this.key = "your-amap-key-here"; // 占位符，实际使用时应通过 setKey() 设置
    }
}