package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 完整书籍结果封装类
 * 包含所有相关实体信息的聚合类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResult {
    private Book book;
    private List<Trajectory> trajectories;
    private List<Chapter> chapters;
    private List<Story> stories;

    //章节和情节关联表
    private List<ChapterStory> chapterStories;
}

//    private List<Role> roles;
//    private List<Environment> environments;