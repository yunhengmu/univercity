package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: BookMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/1/11 09:53
 * @Version 1.0
 */
@Mapper
public interface BookMapper  {
    @Select("SELECT *, turning_point as turningPoint, branch_num as branchNum, " +
            "chapter_num as chapterNum, total_chapter as totalChapter FROM book WHERE id=#{id}")
    Book selectOneById(Long id);

    /**
     * 添加书籍
     * @param book
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into " +
            "book(name,user_id,type,world,style,core,beginning,development,turning_point,climax,ending,wordstype,branch_num,control,chapter_num,total_chapter)" +
            "values(#{name},#{userId},#{type},#{world},#{style},#{core},#{beginning},#{development},#{turningPoint},#{climax},#{ending},#{wordsType},#{branchNum},#{control},#{chapterNum},#{totalChapter})")
    int insert(Book book);

    /**
     * 修改书籍
     * @param book 包含要更新的数据和id
     */
    @Update("UPDATE book SET " +
            "name=#{name}, type=#{type}, world=#{world}, style=#{style}, core=#{core}, " +
            "beginning=#{beginning}, development=#{development}, turning_point=#{turningPoint}, " +
            "climax=#{climax}, ending=#{ending}, wordstype=#{wordsType}, branch_num=#{branchNum}, " +
            "control=#{control}, chapter_num=#{chapterNum}, total_chapter=#{totalChapter} " +
            "WHERE id=#{id}")
    int updateById(Book book);

    @Select("SELECT * FROM book WHERE id=#{id}")
    Book getOne(Integer id);

    /**
     * 查询所有已上架的书籍列表(通过up_log表关联)
     * @return 书籍列表
     */
    List<Book> selectAllShelvedBooks();
    
    /**
     * 分页查询所有已上架的书籍列表(通过up_log表的bookId关联)
     * @return 书籍列表
     */
    List<Book> selectShelvedBooksByUpLog();

    @Select("SELECT chapter FROM book_chapter WHERE book_id=#{bookId}")
    List<String> getChapters(Integer bookId);
}
