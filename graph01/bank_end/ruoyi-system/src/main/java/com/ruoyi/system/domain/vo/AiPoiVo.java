package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.AiAt;
import com.ruoyi.system.domain.AiPoi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: AiPoi
 * Package: com.ruoyi.system.domain
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 09:07
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiPoiVo extends AiPoi {
    List<AiAt> aiareas;
}
