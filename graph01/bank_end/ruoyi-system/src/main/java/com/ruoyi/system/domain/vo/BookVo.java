package com.ruoyi.system.domain.vo;

import com.ruoyi.system.domain.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: BookVo
 * Package: com.ruoyi.system.domain.vo
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 08:18
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookVo extends Book {
    private Integer favoriteId;
    private Integer loveId;
}
