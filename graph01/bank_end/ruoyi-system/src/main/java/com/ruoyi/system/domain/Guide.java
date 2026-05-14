package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: Guide
 * Package: com.ruoyi.system.domain
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 07:50
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guide {
    private Integer id;
    private Integer userId;
    private String name;
}
