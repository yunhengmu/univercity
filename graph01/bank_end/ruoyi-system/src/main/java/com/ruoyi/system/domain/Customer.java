package com.ruoyi.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Customer实体类，对应customer表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Integer id;

    private String name;

    private String description;

    private Boolean status;

    private String imageUrl;

    private String createdAt;

    private String updatedAt;
}