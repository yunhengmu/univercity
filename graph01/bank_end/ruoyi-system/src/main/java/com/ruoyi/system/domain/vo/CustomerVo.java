package com.ruoyi.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: CustomerVo
 * Package: com.ruoyi.system.domain.vo
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/2/9 16:56
 * @Version 1.0
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomerVo {
    private Integer id;

    private String introduction;

    private String nickName;

    private String imageUrl;
}
