package com.ruoyi.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrajectoryBook {
    private Integer id;
    private Integer trajectoryId;
    private Integer bookId;
    private String type;
}