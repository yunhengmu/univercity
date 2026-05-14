package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: Favorite
 * Package: com.ruoyi.system.domain
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 23:01
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    private Integer id;
    private Integer favoriteId;
    private Integer typeId;
    private Integer userId;
}
