package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: AiareaMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 19:17
 * @Version 1.0
 */
@Mapper
public interface AiareaMapper {
    @Delete("delete from aiarea where id = #{id}")
    int deleteAiarea(Integer id);
}