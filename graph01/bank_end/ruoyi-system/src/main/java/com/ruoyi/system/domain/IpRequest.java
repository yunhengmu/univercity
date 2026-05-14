package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpRequest {
    private String ip;
    private String output;
    private String key;

    public IpRequest(String ip) {
        this.ip = ip;
        this.output = "json"; // 固定为 json
        // 注意：实际使用时应该从配置文件或环境变量中获取
        this.key = System.getenv("AMAP_KEY") != null ? System.getenv("AMAP_KEY") : "your-amap-key-here";
    }

}