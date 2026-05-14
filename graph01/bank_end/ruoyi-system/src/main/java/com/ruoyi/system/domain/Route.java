package com.ruoyi.system.domain;

import lombok.Data;

import java.util.List;

@Data
public  class Route {
    private String origin;
    private String destination;
    private String taxiCost;
    private List<Path> paths;

}