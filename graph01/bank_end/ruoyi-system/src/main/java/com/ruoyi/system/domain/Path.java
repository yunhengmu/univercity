package com.ruoyi.system.domain;

import lombok.Data;

import java.util.List;

@Data
public  class Path {
    private String distance;
    private String duration;
    private String strategy;
    private String tolls;
    private String tollDistance;
    private List<Step> steps;
    private String restriction;
    private String trafficLights;
}