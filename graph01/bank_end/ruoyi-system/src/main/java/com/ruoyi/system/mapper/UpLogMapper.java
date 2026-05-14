package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.admin.UpLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: UpLogMapper
 * Package: com.ruoyi.system.mapper
 * Description: 上架记录Mapper接口
 *
 * @Author 张鹏
 * @Create 2026/4/5 10:54
 * @Version 1.0
 */
@Mapper
public interface UpLogMapper {
    
    /**
     * 根据ID查询上架记录
     * @param id 上架记录ID
     * @return 上架记录
     */
    UpLog selectUpLogById(@Param("id") Integer id);
    
    /**
     * 查询上架记录列表
     * @param upLog 查询条件
     * @return 上架记录列表
     */
    List<UpLog> selectUpLogList(UpLog upLog);
    
    /**
     * 查询所有上架记录列表(无参数)
     * @return 上架记录列表
     */
    List<UpLog> selectAllUpLogList();
    
    /**
     * 新增上架记录
     * @param upLog 上架记录
     * @return 影响行数
     */
    int insertUpLog(UpLog upLog);
    
    /**
     * 修改上架记录
     * @param upLog 上架记录
     * @return 影响行数
     */
    int updateUpLog(UpLog upLog);
    
    /**
     * 删除上架记录
     * @param id 上架记录ID
     * @return 影响行数
     */
    int deleteUpLogById(@Param("id") Integer id);
    
    /**
     * 批量删除上架记录
     * @param ids 上架记录ID数组
     * @return 影响行数
     */
    int deleteUpLogByIds(@Param("ids") Integer[] ids);
    
    /**
     * 上架(将状态改为1)
     * @param id 上架记录ID
     * @return 影响行数
     */
    int shelveUpLog(@Param("id") Integer id);
    
    /**
     * 下架(将状态改为0)
     * @param id 上架记录ID
     * @return 影响行数
     */
    int unshelveUpLog(@Param("id") Integer id);
}
