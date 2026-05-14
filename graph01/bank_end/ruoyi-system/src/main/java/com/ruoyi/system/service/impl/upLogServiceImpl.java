package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.admin.UpLog;
import com.ruoyi.system.mapper.BookMapper;
import com.ruoyi.system.mapper.UpLogMapper;
import com.ruoyi.system.service.upLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: upLogServiceImpl
 * Package: com.ruoyi.system.service.impl
 * Description: 上架记录服务实现类
 *
 * @Author 张鹏
 * @Create 2026/4/5 10:53
 * @Version 1.0
 */
@Service
public class upLogServiceImpl implements upLogService {
    
    @Autowired
    private UpLogMapper upLogMapper;
    
    @Autowired
    private BookMapper bookMapper;
    
    /**
     * 根据ID查询上架记录
     * @param id 上架记录ID
     * @return 上架记录
     */
    @Override
    public UpLog selectUpLogById(Integer id) {
        return upLogMapper.selectUpLogById(id);
    }
    
    /**
     * 查询上架记录列表
     * @param upLog 查询条件
     * @return 上架记录列表
     */
    @Override
    public List<UpLog> selectUpLogList(UpLog upLog) {
        System.out.println(upLogMapper.selectUpLogList(upLog));
        return upLogMapper.selectUpLogList(upLog);
    }
    
    /**
     * 查询所有上架记录列表(无参数)
     * @return 上架记录列表
     */
    @Override
    public List<UpLog> selectAllUpLogList() {
        return upLogMapper.selectAllUpLogList();
    }
    
    /**
     * 新增上架记录
     * @param upLog 上架记录
     * @return 影响行数
     */
    @Override
    public int insertUpLog(UpLog upLog) {
        // 设置创建时间
        if (upLog.getCreateTime() == null) {
            upLog.setCreateTime(LocalDateTime.now());
        }
        // 设置更新时间
        if (upLog.getUpdateTime() == null) {
            upLog.setUpdateTime(LocalDateTime.now());
        }
        return upLogMapper.insertUpLog(upLog);
    }
    
    /**
     * 修改上架记录
     * @param upLog 上架记录
     * @return 影响行数
     */
    @Override
    public int updateUpLog(UpLog upLog) {
        // 设置更新时间
        upLog.setUpdateTime(LocalDateTime.now());
        return upLogMapper.updateUpLog(upLog);
    }
    
    /**
     * 删除上架记录
     * @param id 上架记录ID
     * @return 影响行数
     */
    @Override
    public int deleteUpLogById(Integer id) {
        return upLogMapper.deleteUpLogById(id);
    }
    
    /**
     * 批量删除上架记录
     * @param ids 上架记录ID数组
     * @return 影响行数
     */
    @Override
    public int deleteUpLogByIds(Integer[] ids) {
        return upLogMapper.deleteUpLogByIds(ids);
    }
    
    /**
     * 上架(将状态改为1)
     * @param id 上架记录ID
     * @return 影响行数
     */
    @Override
    public int shelveUpLog(Integer id) {
        return upLogMapper.shelveUpLog(id);
    }
    
    /**
     * 下架(将状态改为0)
     * @param id 上架记录ID
     * @return 影响行数
     */
    @Override
    public int unshelveUpLog(Integer id) {
        return upLogMapper.unshelveUpLog(id);
    }
    
    /**
     * 分页查询所有已上架的书籍列表(通过up_log表的bookId关联)
     * @return 书籍列表
     */
    @Override
    public List<Book> selectShelvedBooksByUpLog() {
        return bookMapper.selectShelvedBooksByUpLog();
    }
}
