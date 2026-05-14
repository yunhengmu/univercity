package com.ruoyi.system.service;

import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.admin.UpLog;

import java.util.List;

/**
 * ClassName: upLogService
 * Package: com.ruoyi.system.service
 * Description: 上架记录服务接口
 *
 * @Author 张鹏
 * @Create 2026/4/5 10:52
 * @Version 1.0
 */
public interface upLogService {
    
    /**
     * 根据ID查询上架记录
     * @param id 上架记录ID
     * @return 上架记录
     */
    UpLog selectUpLogById(Integer id);
    
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
    int deleteUpLogById(Integer id);
    
    /**
     * 批量删除上架记录
     * @param ids 上架记录ID数组
     * @return 影响行数
     */
    int deleteUpLogByIds(Integer[] ids);
    
    /**
     * 上架(将状态改为1)
     * @param id 上架记录ID
     * @return 影响行数
     */
    int shelveUpLog(Integer id);
    
    /**
     * 下架(将状态改为0)
     * @param id 上架记录ID
     * @return 影响行数
     */
    int unshelveUpLog(Integer id);
    
    /**
     * 分页查询所有已上架的书籍列表(通过up_log表的bookId关联)
     * @return 书籍列表
     */
    List<Book> selectShelvedBooksByUpLog();
}
