package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 道路交叉口信息
 */
@Data
public class RoadInter implements Serializable {
    private static final long serialVersionUID = 1L;

    private String direction;
    private String distance;
    private String location;

    @JsonProperty("first_id") // 映射JSON的first_id
    private String firstId;

    @JsonProperty("first_name") // 映射JSON的first_name
    private String firstName;

    @JsonProperty("second_id") // 映射JSON的second_id
    private String secondId;

    @JsonProperty("second_name") // 映射JSON的second_name
    private String secondName;
}