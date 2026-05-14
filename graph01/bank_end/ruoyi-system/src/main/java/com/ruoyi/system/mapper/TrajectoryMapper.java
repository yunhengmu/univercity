package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.Trajectory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

/**
 * ClassName: TrajectoryMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/12/23 14:24
 * @Version 1.0
 */
@Repository
@Mapper
public interface TrajectoryMapper {
    /**
     * 添加轨迹
     * @param trajectory
     * @return
     */
    @Insert("insert into trajectory(create_area,end_area,text,create_time,end_time) values(#{createArea},#{endArea},#{text},#{createTime},#{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Trajectory trajectory);

}
