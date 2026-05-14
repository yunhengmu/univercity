package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.NavigationData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: navigationVo
 * Package: com.ruoyi.system.domain.vo
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/12/21 14:44
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationVo {
    NavigationData origin;
    NavigationData destination;
    String type;
    String wordsType;
    String byWay;
    Integer userId;
}
