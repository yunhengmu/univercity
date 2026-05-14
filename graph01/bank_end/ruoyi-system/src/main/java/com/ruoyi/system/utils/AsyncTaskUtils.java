package com.ruoyi.system.utils;

import com.ruoyi.common.core.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class AsyncTaskUtils {

    @Autowired
    private RedisCache redisCache;

    private static final String TASK_PREFIX = "nav_task:";
    private static final long EXPIRE_TIME = 1L;
    private static final TimeUnit TIME_UNIT = TimeUnit.HOURS;

    public String executeAsync(Supplier<?> task) {
        String taskId = UUID.randomUUID().toString();
        redisCache.setCacheObject(TASK_PREFIX + taskId, "PENDING", (int) EXPIRE_TIME, TIME_UNIT);
        
        new Thread(() -> {
            try {
                Object result = task.get();
                redisCache.setCacheObject(TASK_PREFIX + taskId, result, (int) EXPIRE_TIME, TIME_UNIT);
            } catch (Exception e) {
                redisCache.setCacheObject(TASK_PREFIX + taskId, "ERROR: " + e.getMessage(), (int) EXPIRE_TIME, TIME_UNIT);
            }
        }).start();
        
        return taskId;
    }
}
