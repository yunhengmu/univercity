package com.ruoyi.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {
    @description("章节ID")
    private Integer id;
    @description("章节名称")
    private String name;
    @description("章节所属书籍ID")
    private Integer bookId;
    @description("章节字数")
    private Integer words;
    @description("章节内容")
    private String text;
    @description("章节标题")
    private String aword;
}