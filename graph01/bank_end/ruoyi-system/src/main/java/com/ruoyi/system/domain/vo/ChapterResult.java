package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.domain.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: ChapterResult
 * Package: com.ruoyi.system.domain.vo
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/1/11 17:51
 * @Version 1.0
 */
@Data // 自动生成 Getter、Setter、toString、equals、hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterResult {
    private Chapter chapter;
    private Story story;
}
