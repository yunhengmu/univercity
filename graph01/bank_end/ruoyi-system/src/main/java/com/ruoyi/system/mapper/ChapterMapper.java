package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.Chapter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ClassName: ChapterMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/1/11 16:56
 * @Version 1.0
 */
public interface ChapterMapper extends BaseMapper<Chapter> {
    @Select("select * from chapter where book_id=#{bookId}")
    List<Chapter> selectByBookId(Long bookId);

    @Insert("INSERT INTO chapter (book_id,name,words, text, aword) VALUES (#{bookId},#{name}, #{words}, #{text}, #{aword})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Chapter chapter);
}
