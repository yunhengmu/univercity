package com.ruoyi.system.domain;

/**
 * POI查询排序规则枚举
 */
public enum SortRuleEnum {
    /** 按距离排序（默认） */
    DISTANCE("distance"),
    /** 综合排序 */
    WEIGHT("weight");

    private final String value;

    SortRuleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // 兼容字符串转换枚举
    public static SortRuleEnum fromValue(String value) {
        for (SortRuleEnum rule : values()) {
            if (rule.value.equalsIgnoreCase(value)) {
                return rule;
            }
        }
        return DISTANCE; // 默认按距离排序
    }
}