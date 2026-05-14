package com.ruoyi.system.domain;

import lombok.Data;

import java.util.List;

@Data
public  class Step {
    private String instruction;
    private String orientation;
    private String road;
    private String distance;
    private String tolls;
    private String tollDistance;
    private List<String> tollRoad;
    private String duration;
    private String polyline;
    private Object action;// 注意：有时是字符串，有时是空数组 []
    private Object assistantAction; // 注意：有时是字符串，有时是空数组 []
    private List<Tmc> tmcs;
    private List<City> cities;

}