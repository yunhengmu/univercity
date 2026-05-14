package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: AiAt
 * Package: com.ruoyi.system.domain
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 09:08
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAt {
    private Integer id;
    private Integer atPoiId;
    private String contentAi;
}
