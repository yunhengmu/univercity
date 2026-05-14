package com.atguigu.ssyx.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum BillType implements IEnum<Integer> {
    ORDER(0, "订单佣金"),
    WITHDRAW(1, "提现"),
    REFUND(2, "订单退款"); // 注意：值必须唯一！

    @EnumValue // MyBatis-Plus 存储到数据库的值
    @JsonValue // Jackson 序列化的值
    private final Integer code;

    private final String comment;

    BillType(Integer code, String comment) {
        this.code = code;
        this.comment = comment;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }
}