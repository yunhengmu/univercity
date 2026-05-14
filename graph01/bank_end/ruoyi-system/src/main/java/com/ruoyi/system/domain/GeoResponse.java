package com.ruoyi.system.domain;

import java.util.List;

public class GeoResponse {
    private String status;
    private String info;
    private String infocode;
    private Integer count;
    private List<Geocode> geocodes;

    // 内部类：具体地理信息
    public static class Geocode {
        private String formatted_address;
        private String province;
        private String city;
        private String district;
        private String location; // 经纬度 "lng,lat"
        private String adcode;

        // Getters and Setters
        public String getFormatted_address() { return formatted_address; }
        public void setFormatted_address(String formatted_address) { this.formatted_address = formatted_address; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getAdcode() { return adcode; }
        public void setAdcode(String adcode) { this.adcode = adcode; }
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
    public String getInfocode() { return infocode; }
    public void setInfocode(String infocode) { this.infocode = infocode; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public List<Geocode> getGeocodes() { return geocodes; }
    public void setGeocodes(List<Geocode> geocodes) { this.geocodes = geocodes; }
}