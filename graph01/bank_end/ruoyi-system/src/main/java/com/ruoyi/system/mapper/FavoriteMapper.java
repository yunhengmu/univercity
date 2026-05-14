package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Favorite;
import com.ruoyi.system.domain.Guide;
import com.ruoyi.system.domain.vo.BookVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: favoriteMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 23:02
 * @Version 1.0
 */
@Mapper
public interface FavoriteMapper {

    @Insert("insert into favorite(favorite_id,type_id,user_id) values(#{favoriteId},#{typeId},#{userId})")
    void addFavorite(Favorite favorite);

    @Delete("delete from favorite where id = #{id}")
    void deleteFavorite(Integer id);

    @Select("select * from favorite where user_id = #{id}")
    List<Favorite> findFavoritesById(Integer id);

    @Select("SELECT a.*, " +
            "b.favorite_id, " +
            "b.love_id " +
            "FROM book as a " +
            "LEFT JOIN (SELECT favorite_id, id AS love_id FROM favorite WHERE type_id = 0 AND user_id = #{userId}) as b " +
            "ON a.id=b.favorite_id " +
            "WHERE b.love_id IS NOT NULL")
    @Results(id = "bookVoResultMap", value = {
            @Result(property = "favoriteId", column = "favorite_id"),
            @Result(property = "loveId", column = "love_id")
    })
    List<BookVo> findFavoriteBookById(Integer userId);

    @Select("SELECT * FROM guide WHERE user_id = #{userId}")
    @Result(property = "userId", column = "user_id")
    List<Guide> findFavoriteGuideById(Integer userId);
}
