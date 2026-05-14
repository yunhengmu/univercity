package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.Story;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: StoryMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/1/11 10:00
 * @Version 1.0
 */
@Mapper
public interface StoryMapper  {
    // 如需自定义插入逻辑，可使用不同的方法名
    @Insert("insert into story(chapter_id,book_id,text,text_type,status,summary,share,used) values(#{chapterId},#{bookId},#{text},#{textType},#{status},#{summary},#{share},#{used})")
    int insertStory(Story story);

    @Select("select * from story where book_id=#{bookId}")
    List<Story> selectStoryList(Long bookId);

    @Update("update story set text=#{text},text_type=#{textType},status=#{status},summary=#{summary},share=#{share},used=#{used} where id=#{id}")
    void updateItem(Story story);
}
