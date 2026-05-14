package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.Area;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: AreaMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/12/23 15:33
 * @Version 1.0
 */
@Mapper
public interface AreaMapper  {

    @Select("select * from area where trajectory_id = #{trajectoryId}")
    List<Area> selectByTrajectoryId(Integer trajectoryId);

    @Insert("insert into area(trajectory_id,road_name,text,status,created_at,updated_at) values(#{trajectoryId},#{roadName},#{text},#{status},#{createdAt},#{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Area area);


}
