package com.ruoyi.system.domain;

import com.ruoyi.common.config.AmapConfig;

public class WeatherRequest {
    private String city;
    private String extensions;
    private String key;

    public WeatherRequest(String city, String extensions) {
        this.city = city;
        this.extensions = extensions != null ? extensions : "base";
        this.key = AmapConfig.KEY;
    }

    // Getters and Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getExtensions() { return extensions; }
    public void setExtensions(String extensions) { this.extensions = extensions; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}