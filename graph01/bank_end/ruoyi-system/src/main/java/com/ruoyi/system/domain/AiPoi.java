package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: AiPoi
 * Package: com.ruoyi.system.domain
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 09:16
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiPoi {
    private Integer id;
    private String name;
    private Integer guideId;
}
