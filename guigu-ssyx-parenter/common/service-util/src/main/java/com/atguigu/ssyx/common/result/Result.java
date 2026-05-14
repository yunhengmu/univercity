package com.atguigu.ssyx.common.result;

import lombok.Data;

/**
 * ClassName: Result
 * Package: com.atguigu.ssyx.common.result
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/6/23 15:53
 * @Version 1.0
 */
@Data
public class Result <T>{
    private Integer code;
    private String msg;
    private T data;

    private Result(){}

    public static<T> Result<T> Build(T data,ResultCodeEnum resultCodeEnum){
        Result<T> result=new Result<T>();
        if (data!=null){
            result.setData(data);
        }
        result.setCode(resultCodeEnum.getCode());
        result.setMsg(resultCodeEnum.getMessage());
        return result;
    }

    public static <T> Result<T> ok(T data){
        return Build(data, ResultCodeEnum.SUCCESS);
    }

    public static<T> Result<T> fail(T data) {
        return Result.Build(data,ResultCodeEnum.FAIL);
    }

   /* public static <T> Result build(T data, Integer code, String message) {
        Result<T> result=new Result<T>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(data);
        return result;
    }*/
}
