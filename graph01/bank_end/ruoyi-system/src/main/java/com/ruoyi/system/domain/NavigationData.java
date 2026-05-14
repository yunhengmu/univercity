package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ClassName: NavigationData
 * Package: com.ruoyi.system.domain
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/9/12 09:26
 * @Version 1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationData {
    private BigDecimal longitude ;
    private BigDecimal latitude;
}
