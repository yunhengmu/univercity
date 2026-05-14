package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上架书目VO类
 * 用于返回上架记录及其关联的书籍信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShelveBookVo {
    
    // UpLog相关字段
    private Integer upLogId;          // 上架记录ID
    private Integer bookId;           // 书籍ID
    private Integer userId;           // 用户ID
    private Integer status;           // 上架状态（0-未上架 / 1-已上架）
    private LocalDateTime createTime; // 上架时间
    private LocalDateTime updateTime; // 更新时间
    
    // Book相关字段
    private String name;              // 书名
    private String type;              // 书籍类型
    private String world;             // 书籍世界观
    private String style;             // 文笔风格
    private String core;              // 主旨
    private String beginning;         // 开篇
    private String development;       // 发展
    private String turningPoint;      // 转折
    private String climax;            // 高潮
    private String ending;            // 结尾
    private String wordsType;         // 文章长度
    private Integer branchNum;        // 支线数量
    private String control;           // 篇幅控制
    private Integer chapterNum;       // 目前进行章数
    private Integer totalChapter;     // 小说总章数
}
