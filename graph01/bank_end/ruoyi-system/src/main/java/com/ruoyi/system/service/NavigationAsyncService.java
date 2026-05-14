package com.ruoyi.system.service;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.system.domain.vo.NavigationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class NavigationAsyncService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private StartNavigationService startNavigationService;

    @Async("threadPoolTaskExecutor")  // 使用若依已有的线程池
    public void executeTask(String taskId, NavigationVo vo) {
        try {
            // 调用你原来的耗时方法
            Map<String, Long> result = startNavigationService.getTextCloud(
                    vo.getOrigin(),
                    vo.getDestination(),
                    vo.getType(),
                    vo.getWordsType(),
                    vo.getByWay(),
                    vo.getUserId()
            );
            // 将结果存入 Redis，有效期 1 小时
            redisCache.setCacheObject("nav:task:" + taskId, result, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            // 如果出错，存错误信息
            redisCache.setCacheObject("nav:task:" + taskId, "ERROR: " + e.getMessage(), 1, TimeUnit.HOURS);
        }
    }
}