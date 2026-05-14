package com.ruoyi.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmapConfig {
    // 从环境变量或配置文件中读取，不要硬编码
    @Value("${amap.key:your-amap-key-here}")
    public String key;
    
    public static final String GEO_URL = "https://restapi.amap.com/v3/geocode/geo";
    public static final String RE_GEO_URL = "https://restapi.amap.com/v3/geocode/regeo";
    public static final String WEATHER_URL = "https://restapi.amap.com/v3/weather/weatherInfo";
    public static final String DIRECTION_URL = "https://restapi.amap.com/v3/direction/driving";
    public static final String IP_URL = "https://restapi.amap.com/v3/ip";
    public static final String POI_URL = "https://restapi.amap.com/v3/place/around";
}