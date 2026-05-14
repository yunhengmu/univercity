package com.ruoyi.system.domain;

import lombok.Data;
import java.util.List;

/**
 * 图片信息
 */
@Data
public class Photo {
    /** 标题 */
    private List<String> title;
    /** 图片URL */
    private String url;
}