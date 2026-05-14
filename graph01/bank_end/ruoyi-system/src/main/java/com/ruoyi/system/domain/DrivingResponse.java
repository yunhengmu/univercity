package com.ruoyi.system.domain;

public class DrivingResponse {
    private String status;
    private String info;
    private String infocode;
    private Route route;



    // Outer Getters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
    public String getInfocode() { return infocode; }
    public void setInfocode(String infocode) { this.infocode = infocode; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }
}