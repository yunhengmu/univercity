package com.ruoyi.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: chapterVo
 * Package: com.ruoyi.system.domain.vo
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/2/9 10:20
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterVo {
    Long bookId;
    Integer trajectoryId;
    Integer length;
}
