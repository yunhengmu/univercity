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
        this.key = "cbd2f2cb175d650a4c6db70b96dc656b"; // 使用你的 key
    }

}