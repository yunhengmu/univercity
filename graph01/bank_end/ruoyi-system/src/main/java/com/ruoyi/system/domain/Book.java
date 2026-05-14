package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Book {
    @description("书籍ID")
    private Integer id;
    @description("书籍名称")
    private String name;//书名
    private Integer userId;
    @description("书籍类型")
    private String type;//书籍类型
    @description("书籍世界观")
    private String world;//书籍世界观
    @description("文笔风格")
    private String style;//文笔风格
    @description("书籍主旨")
    private String core;//主旨
    @description("书籍开篇")
    private String beginning;//开篇
    @description("书籍发展")
    private String development;//发展
    @description("书籍转折点")
    private String turningPoint;//转折
    @description("书籍高潮")
    private String climax;//高潮
    @description("书籍结尾")
    private String ending;//结尾
    @description("文章长度")
    private String wordsType;//文章长度
    @description("支线数量")
    private Integer branchNum;
    @description("开篇，发展，转折，高潮，结尾对应的篇幅")
    private String control;
    @description("目前进行章数")
    private Integer chapterNum;
    @description("小说总共有多少章数")
    private Integer totalChapter;
}