package com.ruoyi.system.service;

import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Favorite;
import com.ruoyi.system.domain.Guide;
import com.ruoyi.system.domain.vo.BookVo;

import java.util.List;

/**
 * ClassName: FavoriteServicce
 * Package: com.ruoyi.system.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 23:10
 * @Version 1.0
 */
public interface FavoriteService {

    String addFavorite(Favorite favorite);

    void deleteFavorite(Integer id);


    List<Guide> getFavoritesGuide(Integer userId);

    List<BookVo> getFavoritesBook(Integer userId);

    void deleteGuide(Integer id);
}
