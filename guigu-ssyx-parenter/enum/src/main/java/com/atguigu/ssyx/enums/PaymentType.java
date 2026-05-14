package com.atguigu.ssyx.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PaymentType {
    ALIPAY(1,"支付宝"),
    WEIXIN(2,"微信" );

    @EnumValue
    private final Integer code ;
    private final String comment ;

    PaymentType(Integer code, String comment ){
        this.code = code;
        this.comment=comment;
    }

}
