package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 地点表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {
    @description("位置ID")
    private Long id;
    @description("轨迹ID")
    private Long trajectoryId;
    @description("初始位置到该位置所花费的时间")
    private String duration;
    @description("地点名称")
    private String roadName;
    @description("对这个位置的描述")
    private String text;
    @description("状态，是否可以使用，如果是0不可使用，如果是1可以使用")
    private Integer status;
    @description("创建时间")
    private LocalDateTime createdAt;
    @description("更新时间")
    private LocalDateTime updatedAt;
}