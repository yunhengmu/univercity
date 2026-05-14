package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("trajectory")
public class Trajectory extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @description("轨迹起始点")
    private String createArea;
    @description("轨迹终点")
    private String endArea;
    @description("轨迹描述")
    private String text;
    private Date createTime;
    private Date endTime;
}