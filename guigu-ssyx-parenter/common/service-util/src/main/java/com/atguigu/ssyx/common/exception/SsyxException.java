package com.atguigu.ssyx.common.exception;

import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import lombok.Data;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * ClassName: SsyxException
 * Package: com.atguigu.ssyx.common.exception
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/6/23 19:12
 * @Version 1.0
 */
@Data
public class SsyxException extends RuntimeException {

    //异常状态码
    private Integer code;
    /**
     * 通过状态码和错误消息创建异常对象
     * @param message
     * @param code
     */
    public SsyxException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public SsyxException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
