package com.ruoyi.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 环境-地点关联表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentArea {
    private Integer id;
    private Integer environmentId;
    private Integer areaId;
    private LocalDateTime createdAt;
}