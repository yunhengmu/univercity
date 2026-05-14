package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.Need;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: NeedMapper
 * Package: com.ruoyi.system.mapper
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 21:46
 * @Version 1.0
 */
@Mapper
public interface NeedMapper {

    /**
     * 根据用户 ID 查询需求列表
     * @param userId 用户 ID
     * @return 需求列表
     */
    List<Need> selectNeedsByUserId(@Param("userId") Integer userId);

    /**
     * 插入需求
     * @param need 需求对象
     * @return 影响行数
     */
    int insert(Need need);

    /**
     * 根据 ID 删除需求
     * @param id 需求 ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新需求
     * @param need 需求对象
     * @return 影响行数
     */
    int update(Need need);

    /**
     * 根据 ID 更新需求
     * @param need 需求对象
     * @return 影响行数
     */
    void updateById(Need need);
}
