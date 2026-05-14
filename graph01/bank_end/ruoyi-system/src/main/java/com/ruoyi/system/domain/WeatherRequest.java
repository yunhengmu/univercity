package com.ruoyi.system.domain;

import com.ruoyi.common.config.AmapConfig;

public class WeatherRequest {
    private String city;
    private String extensions;
    private String key;

    public WeatherRequest(String city, String extensions) {
        this.city = city;
        this.extensions = extensions != null ? extensions : "base";
        // 注意：key 应该在调用方设置，从配置中获取
        this.key = "your-amap-key-here"; // 占位符，实际使用时应通过 setKey() 设置
    }

    // Getters and Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getExtensions() { return extensions; }
    public void setExtensions(String extensions) { this.extensions = extensions; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}