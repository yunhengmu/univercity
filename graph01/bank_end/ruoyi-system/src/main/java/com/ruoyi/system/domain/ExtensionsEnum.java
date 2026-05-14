package com.ruoyi.system.domain;

/**
 * POI返回结果控制枚举
 */
public enum ExtensionsEnum {
    /** 仅返回基本地址信息（默认） */
    BASE("base"),
    /** 返回完整信息（地址+附近POI+道路+交叉口） */
    ALL("all");

    private final String value;

    ExtensionsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // 兼容字符串转换枚举
    public static ExtensionsEnum fromValue(String value) {
        for (ExtensionsEnum ext : values()) {
            if (ext.value.equalsIgnoreCase(value)) {
                return ext;
            }
        }
        return BASE; // 默认返回基础信息
    }
}