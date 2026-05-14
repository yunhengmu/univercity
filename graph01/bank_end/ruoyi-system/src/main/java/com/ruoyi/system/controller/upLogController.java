package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.admin.UpLog;
import com.ruoyi.system.mapper.BookMapper;
import com.ruoyi.system.service.UserDataService;
import com.ruoyi.system.service.upLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: upLogController
 * Package: com.ruoyi.system.controller
 * Description: 上架记录控制器
 *
 * @Author 张鹏
 * @Create 2026/4/5 10:52
 * @Version 1.0
 */
@Tag(name = "上架记录管理", description = "上架记录的增删改查接口")
@RestController
@RequestMapping("/upLog")
@Anonymous
public class upLogController extends BaseController {
    
    @Autowired
    private upLogService upLogService;
    
    @Autowired
    private BookMapper bookMapper;

    @GetMapping("/getOneBook")
    public AjaxResult getOneBook(Integer bookId) {
        Book book = bookMapper.getOne(bookId);
        return success(book);
    }

    @GetMapping("getChapters")
    public AjaxResult getChapters(Integer bookId) {
        startPage();
        List<String> chapters = bookMapper.getChapters(bookId);
        return success(chapters);
    }

    /**
     * 分页查询上架记录列表
     * @param upLog 查询条件
     * @return 分页数据
     */
    @Operation(summary = "分页查询上架记录列表", description = "支持根据bookId、userId、status等条件筛选")
    @GetMapping("/list")
    public TableDataInfo list(UpLog upLog) {
        startPage();
        List<UpLog> list = upLogService.selectUpLogList(upLog);
        System.out.println("list"+ list);
        return getDataTable(list);
    }
    
    /**
     * 分页查询所有上架记录(无参数)
     * @return 分页数据
     */
    @Operation(summary = "分页查询所有上架记录", description = "不带任何筛选条件,返回全部上架记录")
    @GetMapping("/all")
    public TableDataInfo listAll() {
        startPage();
        List<UpLog> list = upLogService.selectAllUpLogList();
        return getDataTable(list);
    }
    
    /**
     * 根据ID查询上架记录详情
     * @param id 上架记录ID
     * @return 上架记录详情
     */
    @Operation(summary = "查询上架记录详情", description = "根据ID获取单条上架记录信息")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Integer id) {
        UpLog upLog = upLogService.selectUpLogById(id);
        if (upLog == null) {
            return error("上架记录不存在");
        }
        return success(upLog);
    }
    
    /**
     * 新增上架记录
     * @param upLog 上架记录信息
     * @return 操作结果
     */
    @Operation(summary = "新增上架记录", description = "创建新的上架记录")
    @PostMapping
    public AjaxResult add(@RequestBody UpLog upLog) {
        int rows = upLogService.insertUpLog(upLog);
        return toAjax(rows);
    }
    
    /**
     * 修改上架记录
     * @param upLog 上架记录信息
     * @return 操作结果
     */
    @Operation(summary = "修改上架记录", description = "更新已存在的上架记录")
    @PutMapping
    public AjaxResult edit(@RequestBody UpLog upLog) {
        if (upLog.getId() == null) {
            return error("上架记录ID不能为空");
        }
        int rows = upLogService.updateUpLog(upLog);
        return toAjax(rows);
    }
    
    /**
     * 删除上架记录
     * @param id 上架记录ID
     * @return 操作结果
     */
    @Operation(summary = "删除上架记录", description = "根据ID删除单条上架记录")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Integer id) {
        int rows = upLogService.deleteUpLogById(id);
        return toAjax(rows);
    }
    
    /**
     * 批量删除上架记录
     * @param ids 上架记录ID数组
     * @return 操作结果
     */
    @Operation(summary = "批量删除上架记录", description = "根据多个ID批量删除上架记录")
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody Integer[] ids) {
        if (ids == null || ids.length == 0) {
            return error("请选择要删除的数据");
        }
        int rows = upLogService.deleteUpLogByIds(ids);
        return toAjax(rows);
    }
    
    /**
     * 上架(将状态改为1)
     * @param id 上架记录ID
     * @return 操作结果
     */
    @Operation(summary = "上架", description = "将上架记录状态改为已上架(1)")
    @PutMapping("/shelve/{id}")
    public AjaxResult shelve(@PathVariable Integer id) {
        UpLog upLog = upLogService.selectUpLogById(id);
        if (upLog == null) {
            return error("上架记录不存在");
        }
        if (upLog.getStatus() != null && upLog.getStatus() == 1) {
            return warn("该记录已经是上架状态");
        }
        int rows = upLogService.shelveUpLog(id);
        return toAjax(rows);
    }
    
    /**
     * 下架(将状态改为0)
     * @param id 上架记录ID
     * @return 操作结果
     */
    @Operation(summary = "下架", description = "将上架记录状态改为未上架(0)")
    @PutMapping("/unshelve/{id}")
    public AjaxResult unshelve(@PathVariable Integer id) {
        UpLog upLog = upLogService.selectUpLogById(id);
        if (upLog == null) {
            return error("上架记录不存在");
        }
        if (upLog.getStatus() != null && upLog.getStatus() == 0) {
            return warn("该记录已经是下架状态");
        }
        int rows = upLogService.unshelveUpLog(id);
        return toAjax(rows);
    }
    
    /**
     * 分页查询所有已上架的书籍列表
     * @return 分页数据
     */
    @Operation(summary = "分页查询所有已上架书籍", description = "通过up_log表的bookId关联查询所有已上架的书籍")
    @GetMapping("/books")
    public TableDataInfo getShelvedBooks() {
        startPage();
        List<Book> list = bookMapper.selectAllShelvedBooks();
        return getDataTable(list);
    }
    
    /**
     * 提交上架记录
     * @param bookId 书籍ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "提交上架记录", description = "创建新的上架记录,status默认为0(未上架),时间自动生成")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestParam Integer bookId, @RequestParam Integer userId) {
        if (bookId == null || userId == null) {
            return error("书籍ID和用户ID不能为空");
        }
        
        UpLog upLog = new UpLog();
        upLog.setBookId(bookId);
        upLog.setUserId(userId);
        upLog.setStatus(0); // 默认未上架状态
        // createTime和updateTime会在service层自动生成
        
        int rows = upLogService.insertUpLog(upLog);
        return toAjax(rows);
    }

}
