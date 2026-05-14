package com.ruoyi.system.service.impl;

import com.ruoyi.system.constants.LoveType;
import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Favorite;
import com.ruoyi.system.domain.Guide;
import com.ruoyi.system.domain.vo.BookVo;
import com.ruoyi.system.mapper.FavoriteMapper;
import com.ruoyi.system.mapper.GuideMapper;
import com.ruoyi.system.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: FavoriteServiceImpl
 * Package: com.ruoyi.system.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 23:11
 * @Version 1.0
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private GuideMapper guideMapper;

    @Override
    public String addFavorite(Favorite favorite) {
        if (favorite.getTypeId()==0){
            favoriteMapper.addFavorite(favorite);
            return "success";
        }else if (favorite.getTypeId()==1){
            favoriteMapper.addFavorite(favorite);
            return "success";
        }
        return "返回值有问题";

    }

    @Override
    public void deleteFavorite(Integer id) {
        favoriteMapper.deleteFavorite(id);
    }

    @Override
    public List<BookVo> getFavoritesBook(Integer userId) {
        return favoriteMapper.findFavoriteBookById(userId);
    }

    @Override
    public void deleteGuide(Integer id) {
        guideMapper.deleteAiarea(id);
        guideMapper.deleteGuide(id);

    }


    @Override
    public List<Guide> getFavoritesGuide(Integer userId) {
        System.out.println(favoriteMapper.findFavoriteGuideById(userId));
        return favoriteMapper.findFavoriteGuideById(userId);
    }


}
