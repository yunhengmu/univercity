package com.ruoyi.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 环境表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Environment {
    @description("环境ID")
    private Integer id;
    @description("故事ID")
    private Integer storyId;
    @description("时间描述")
    private String time;
    @description("时间细节描述")
    private String timeText;
    @description("地点细节描述")
    private String areaText;
    @description("自然条件描述")
    private String naturalConditions;
    @description("社会条件描述")
    private String socialConditions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}