package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.domain.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: CustomerMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/2/1 12:19
 * @Version 1.0
 */
public interface CustomerMapper {
    @Select("select * from customer where id = #{userId}")
    @Results(
            value = {
                    @Result(column = "id", property = "id"),
                    @Result(column = "name", property = "name"),
                    @Result(column = "description", property = "description"),
                    @Result(column = "image_url", property = "imageUrl"),
                    @Result(column = "status", property = "status"),
                    @Result(column = "created_at", property = "createdAt"),
                    @Result(column = "updated_at", property = "updatedAt")
            }
    )
    Customer getUserId(Integer userId);

    @Update("update customer set name = #{name}, description = #{description}, image_url = #{imageUrl}, status = #{status}, updated_at = #{updatedAt} where id = #{id}")
    int upUser(Customer customer);

    @Select("select * from book where " +
            "" +
            "type like CONCAT('%', #{tags}, '%')")
    List<Book> getBookByTags(Integer bookId, String tags);

    @Delete("delete from book where id = #{bookId}")
    int deleteBook(Integer bookId);

    @Select("select * from book where user_id = #{userId} limit #{offset}, #{limit}")
    List<Book> getBookByUserId(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("select * from chapter where book_id = #{bookId} limit #{offset}, #{limit} ")
    List<Chapter> getChapters(@Param("bookId") Integer bookId, @Param("offset") Integer offset, @Param("limit") Integer limit);
}
