package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.Aiarea;
import com.ruoyi.system.domain.Guide;
import com.ruoyi.system.domain.vo.BookVo;
import com.ruoyi.system.domain.vo.GuideVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: GuideVoMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 12:42
 * @Version 1.0
 */
@Mapper
public interface GuideMapper {
    void batchInsertAiarea(@Param("aiareas") List<Aiarea> aiareas, @Param("guideId") Integer guideId);

    @Insert("insert into guide (user_id, name) values (#{userId}, #{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Guide guide);

    @Select("select * from guide where user_id = #{id}")
    @Results(id = "guide", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "name", column = "name")
    })
    List<Guide> getGuide(Integer id);

    @Delete("delete from guide where id = #{id}")
    void deleteGuide(Integer id);

    @Delete("delete from aiarea where guide_id = #{id}")
    void deleteAiarea(Integer id);

    @Select("select * from aiarea where guide_id = #{id}")
    @Results(id = "aiarea", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "guideId", column = "guide_id"),
            @Result(property = "contentAi", column = "content_ai"),
            @Result(property = "type", column = "type")
    })
    List<Aiarea> getAiAreaBook(Integer id);
}
