package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.domain.Customer;
import com.ruoyi.system.domain.vo.ChapterVo;
import com.ruoyi.system.domain.vo.CustomerVo;
import com.ruoyi.system.domain.vo.NavigationVo;
import com.ruoyi.system.service.StartNavigationService;
import com.ruoyi.system.service.UserDataService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/")
@Schema(description = "导航数据对象")
@Anonymous
public class GraphController {
    @Autowired
    private StartNavigationService startNavigationService;


    @Autowired
    private UserDataService userDataService;

    @Autowired
    private RedisCache redisCache;

    @DeleteMapping("deleteRedis")
    public String deleteRedis(@RequestParam Integer userId) {
        return startNavigationService.deleteRedis(userId);
    }

    @PostMapping("isNavigation")
    public String isNavigation(Integer userId,Boolean isNavigation) {
        return startNavigationService.isNavigation(userId, isNavigation);
    }

    @PostMapping("startNavigationNoBook")
    public Long startNavigationNoBook(@RequestBody NavigationVo navigationVo) {
        return startNavigationService.getTextCloudNoBook(
                navigationVo.getOrigin(),
                navigationVo.getDestination(),
                navigationVo.getByWay()
        );
    }

    @GetMapping("getPoi")
    public Map<String, String> getPoi(
            @Parameter(description = "经度") @RequestParam String longitude,
            @Parameter(description = "纬度") @RequestParam String latitude,
            @Parameter(description = "用户id") @RequestParam Integer userId,
            @Parameter(description = "缓存键（可选）") @RequestParam(required = false) String cacheKey,
            @Parameter(description = "个性化内容(可选)") @RequestParam(required = false) String content
            ) {
        return startNavigationService.getPoi(
                longitude,
                latitude,
                userId,
                cacheKey,
                content
        );
    }



    @GetMapping("getArea")
    public Map<String, String> getArea(
            @Parameter(description = "经度") @RequestParam String longitude,
            @Parameter(description = "纬度") @RequestParam String latitude,
            @Parameter(description = "用户id") @RequestParam Integer userId,
            @Parameter(description = "缓存键（可选）") @RequestParam(required = false) String cacheKey,
            @Parameter(description = "个性化内容(可选)") @RequestParam(required = false) String content) {
        return startNavigationService.getArea(
                longitude,
                latitude,
                userId,
                cacheKey,
                content
        );
    }

    @PostMapping("get-user")
    public String getUser(@RequestBody NavigationVo navigationVo) {
        return startNavigationService.getText(
                navigationVo.getOrigin(),
                navigationVo.getDestination(),
                navigationVo.getType(),
                navigationVo.getWordsType()
        );//后俩参数为了方便调用使用
    }

    @PostMapping("get-chapter")
    public String getChapter(@RequestBody ChapterVo chapterVo) {
        // 处理不同类型的输入
        return startNavigationService.getChapter(
                chapterVo.getBookId(),
                chapterVo.getTrajectoryId(),
                chapterVo.getLength()
        );
    }

    @GetMapping("getUser")
    public Customer getUser(@RequestParam Integer userId) {
        return userDataService.getUser(userId);
    }

    @PostMapping("updateUser")
    public String updateUser(@RequestBody CustomerVo customer) {
        userDataService.updateUser(customer);
        return "更新用户成功";
    }

    @GetMapping("getBookByTags")
    public List<Book> getBookByTags(Integer userId, @RequestParam String tags) {
        return userDataService.getBookByTags(userId, tags);
    }

    @DeleteMapping("deleteBook")
    public String deleteBook(Integer bookId) {
        return userDataService.deleteBook(bookId);
    }

    @GetMapping("getBooks")
    public List<Book> getBooks(Integer userId, Integer pageNum, Integer pageSize) {
        return userDataService.getBooks(userId, pageNum, pageSize);
    }

    @GetMapping("getChapters")
    public List<Chapter> getChapters(Integer bookId, Integer pageNum, Integer pageSize) {
        return userDataService.getChapters(bookId, pageNum, pageSize);
    }
}