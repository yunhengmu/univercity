package com.ruoyi.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 角色表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @description("角色id")
    private Integer id;
    @description("故事id")
    private Integer storyId;
    @description("角色形象描述")
    private String text;
    @description("角色的内心追求")
    private String want;
    @description("未来角色的追求被满足的情况")
    private String need;
    @description("角色的性格")
    private String personality;
    @description("角色的道德标准，即什么是能做，什么不能做")
    private String ethics;
    @description("角色的价值观")
    private String value;
    @description("角色的经历，人生细节的阐述")
    private String experience;
    @description("角色的伤痕，如果人生平淡可以没有")
    private String scar;
    @description("角色的外貌，比如长相，外貌")
    private String appearance;
    @description("角色社交身份")
    private String socialIdentity;
    @description("角色技能")
    private String skillsAbilities;
    @description("角色语言风格")
    private String languageStyle;
    @description("角色行为习惯")
    private String behavioralHabits;
    @description("角色外部标签，即别人怎么看他")
    private String externalLabel;
    @description("角色执念，和伤痕有区别，可以没有")
    private String arc;
    @description("是否展示")
    private Integer show;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @description("角色涉及情节总结")
    private String aText;
}