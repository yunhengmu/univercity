package com.atguigu.ssyx.common.exception;

import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import org.apache.ibatis.mapping.ResultMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ClassName: GlobalExceptionHandler
 * Package: com.atguigu.ssyx.common.exception
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/6/23 19:07
 * @Version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
     @ExceptionHandler(Exception.class)
     @ResponseBody
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.fail(null);
    }

    @ExceptionHandler(SsyxException.class)
    @ResponseBody
    public Result handleException(SsyxException e) {
        return Result.fail(null);
    }

}
