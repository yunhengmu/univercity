package com.ruoyi.system.domain;

import lombok.Data;

import java.util.List;

@Data
                public  class City {
                    private String name;
                    private String citycode;
                    private String adcode;
                    private List<District> districts;


                }