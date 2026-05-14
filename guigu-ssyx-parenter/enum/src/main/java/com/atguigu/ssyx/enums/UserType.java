package com.atguigu.ssyx.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserType implements IEnum<Integer> {
    USER(0,"会员"),
    LEADER(1,"团长" );

    @EnumValue // MyBatis-Plus 存储到数据库的值
    @JsonValue // Jackson 序列化的值
    private final Integer code ;
    private final String comment ;

    UserType(Integer code, String comment ){
        this.code=code;
        this.comment=comment;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }
}