package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 故事内容表实体类
 * 存储各章节的故事文本片段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {
    /**
     * 故事主键ID，自增唯一标识
     */
    @description("故事主键ID，自增唯一标识")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 章节ID，关联章节表
     */
    @description("章节ID，关联章节表")
    private Integer chapterId;

    /**
     * bookID，关联故事表
     */
    @description("bookID，关联故事表")
    private Integer bookId;

    /**
     * 故事文本内容
     */
    @description("故事的文本内容简短描述，负责在一章内同其他故事融合")
    private String text;
    
    /**
     * 文本类型（开篇/中间/转折/高潮/结尾）
     */
    @description("文本类型（开篇/中间/转折/高潮/结尾），即文本内容描述是这个情节的开篇，中间，转折，高潮，还是结尾，如果是开篇，依据" +
            "大纲内容，来进行类型更改")
    private String textType;
    
    /**
     * 状态：0-未发布 1-已发布
     */
    @description("如果文本内容已经发展了结尾之后，那么这个情节就结束了")
    private Integer status;

    /**
     * 支线大致总结包括篇幅所占篇幅
     */
    @description("支线大致总结包括篇幅所占篇幅")
    private String summary;

    /*
    * 共占篇幅数量
    * */
    @description("共占篇幅数量")
    private Integer share;

    /**
     * 已占篇幅数量
     */
    @description("已占篇幅数量")
    private Integer used;
}