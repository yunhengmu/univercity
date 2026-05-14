package com.ruoyi.system.domain;

import lombok.Data;
import java.util.List;

/**
 * 业务扩展信息
 */
@Data
public class BizExt {
    /** 费用 */
    private List<String> cost;
    /** 评分 */
    private String rating;
}