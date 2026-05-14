package com.ruoyi.system.domain;

import com.ruoyi.common.config.AmapConfig;
import com.ruoyi.system.domain.gaode.GaoDeRegeocodeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AmapService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AmapConfig amapConfig;

    // 1. 获取地理编码
    public GeoResponse getGeoLocation(String address) {
        GeoRequest request = new GeoRequest(address);
        request.setKey(amapConfig.key);
        return restTemplate.getForObject(
            AmapConfig.GEO_URL + "?address={address}&key={key}",
            GeoResponse.class, request.getAddress(), request.getKey()
        );
    }

    // 2. 获取天气信息
    public WeatherResponse getWeather(String cityCode, String extensions) {
        WeatherRequest request = new WeatherRequest(cityCode, extensions);
        request.setKey(amapConfig.key);
        return restTemplate.getForObject(
            AmapConfig.WEATHER_URL + "?city={city}&extensions={extensions}&key={key}",
            WeatherResponse.class, request.getCity(), request.getExtensions(), request.getKey()
        );
    }

    // 3. 获取驾车路线
    public RouteResponse  getDrivingRoute(String origin, String destination) {
        DrivingRequest request = new DrivingRequest(origin, destination);
        request.setKey(amapConfig.key);
        return restTemplate.getForObject(
            AmapConfig.DIRECTION_URL + 
            "?origin={origin}&destination={destination}&extensions={extensions}&output={output}&key={key}",
            RouteResponse .class,
            request.getOrigin(), request.getDestination(),
            request.getExtensions(), request.getOutput(), request.getKey()
        );
    }

    // 4. 根据 IP 获取位置信息
    public IpResponse getIpLocation(String ip) {
        IpRequest request = new IpRequest(ip);
        System.out.println("request:"+request);
        return restTemplate.getForObject(
                AmapConfig.IP_URL + "?ip={ip}&output={output}&key={key}",
                IpResponse.class,
                request.getIp(), request.getOutput(), request.getKey()
        );
    }
    // 5. 获取 POI 列表
    public PoiResponse getPoiList(String address, String city, String types, String radius) {
        AmapPlaceAroundRequest request = new AmapPlaceAroundRequest();
        request.builder()
                .key(amapConfig.key)
                .location(address)
                .city(city)
                .types(types);

        return restTemplate.getForObject(
                AmapConfig.POI_URL + "?key={key}&location={location}&types={types}&radius={radius}&offset={offset}&page={page}&extensions=all",
                PoiResponse.class,
                request.getKey(),
                request.getLocation(),
                request.getTypes(),
                radius,  // radius参数，默认1000米
                "20",    // offset参数，每页记录数
                "1"      // page参数，页码
        );
    }
    // 6. 获取 逆地址编码
    public GaoDeRegeocodeResponse getReverseGeo(String location, Integer radius) {
        // 获取原始响应字符串而不是直接转换为对象

        ReverseGeoRequest request = new ReverseGeoRequest();
        request.setKey(amapConfig.key);
        request.setLocation(location);
        request.setRadius(radius);
        request.setRoadlevel(0);
        request.setExtensions("all");

        return restTemplate.getForObject(
                AmapConfig.RE_GEO_URL + "?key={key}&location={location}&extensions={extensions}&radius={radius}&roadlevel={roadlevel}",
                GaoDeRegeocodeResponse.class,
                request.getKey(),
                request.getLocation(),
                request.getExtensions(),
                request.getRadius(),
                request.getRoadlevel()
        );
    }
}