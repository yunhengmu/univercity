package com.ruoyi.system.domain;

import lombok.Data;

import java.util.List;

@Data
                public  class Tmc {
                    private List<String> lcode;
                    private String distance;
                    private String status;
                    private String polyline;
                }