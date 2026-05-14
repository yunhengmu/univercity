package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.AiPoi;
import com.ruoyi.system.domain.Aiarea;
import com.ruoyi.system.domain.Guide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: GuideVo
 * Package: com.ruoyi.system.domain.vo
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 07:52
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuideVo extends Guide {
    List<Aiarea> aiareas;
}
