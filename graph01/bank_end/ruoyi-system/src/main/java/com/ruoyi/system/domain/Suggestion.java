package com.ruoyi.system.domain;

import lombok.Data;
import java.util.List;

/**
 * 建议信息
 */
@Data
public class Suggestion {
    /** 关键词建议 */
    private List<String> keywords;
    /** 城市建议 */
    private List<String> cities;
}