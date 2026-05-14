package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.system.domain.vo.ChapterVo;
import com.ruoyi.system.domain.vo.NavigationVo;
import com.ruoyi.system.service.StartNavigationService;
import com.ruoyi.system.utils.AsyncTaskUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: ExcuteController
 * Package: com.ruoyi.system.controller
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/6 17:03
 * @Version 1.0
 */
@RestController
@RequestMapping("/excute/")
@Schema(description = "导航数据对象")
@Anonymous
public class ExcuteController {
    @Autowired
    private StartNavigationService startNavigationService;
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private AsyncTaskUtils asyncTaskUtils;


    @PostMapping("start-navigation")
    public R<String> startNavigation(@RequestBody NavigationVo vo) {
        String taskId = asyncTaskUtils.executeAsync(() ->
                startNavigationService.getTextCloud(
                        vo.getOrigin(), vo.getDestination(), vo.getType(),
                        vo.getWordsType(), vo.getByWay(), vo.getUserId())
        );
        return R.ok(taskId);
    }

    @PostMapping("get-chapter")
    public R<String> getChapter(@RequestBody ChapterVo chapterVo) {
        String taskId = asyncTaskUtils.executeAsync(() ->
                startNavigationService.getChapter(
                        chapterVo.getBookId(),
                        chapterVo.getTrajectoryId(),
                        chapterVo.getLength()
                )
        );
        return R.ok(taskId);
    }

    @GetMapping("getArea")
    public R<Map<String, String>> getArea(
            @Parameter(description = "经度") @RequestParam String longitude,
            @Parameter(description = "纬度") @RequestParam String latitude,
            @Parameter(description = "用户id") @RequestParam Integer userId,
            @Parameter(description = "缓存键（可选）") @RequestParam(required = false) String cacheKey,
            @Parameter(description = "个性化内容(可选)") @RequestParam(required = false) String content) {
        String taskId = asyncTaskUtils.executeAsync(() ->
                startNavigationService.getArea(longitude, latitude, userId, cacheKey, content)
        );
        return R.ok(taskId);
    }
    @GetMapping("getPoi")
    public R<Map<String, String>> getPoi(
            @Parameter(description = "经度") @RequestParam String longitude,
            @Parameter(description = "纬度") @RequestParam String latitude,
            @Parameter(description = "用户id") @RequestParam Integer userId,
            @Parameter(description = "缓存键（可选）") @RequestParam(required = false) String cacheKey,
            @Parameter(description = "个性化内容(可选)") @RequestParam(required = false) String content
    ) {
        String taskId = asyncTaskUtils.executeAsync(() ->
                startNavigationService.getPoi(longitude, latitude, userId, cacheKey, content)
        );
        return R.ok(taskId);
    }
    @GetMapping("task")
    public R<Object> getTaskResult(@RequestParam String taskId) {
        System.out.println("获取Redis: nav_task:" + taskId);
        Object obj = redisCache.getCacheObject("nav_task:" + taskId);
        if (obj == null) {
            return R.fail("任务不存在或已过期");
        }
        if ("PENDING".equals(obj)) {
            return R.ok("PENDING");
        }
        return R.ok(obj);
    }
}
