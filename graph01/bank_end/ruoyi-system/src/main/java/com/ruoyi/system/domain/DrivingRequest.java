package com.ruoyi.system.domain;

import com.ruoyi.common.config.AmapConfig;

public class DrivingRequest {
    private String origin;
    private String destination;
    private String extensions;
    private String output;
    private String key;

    public DrivingRequest(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
        this.extensions = "all";
        this.output = "json";
        this.key = AmapConfig.KEY;
    }

    // Getters and Setters
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getExtensions() { return extensions; }
    public void setExtensions(String extensions) { this.extensions = extensions; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}