package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.system.domain.Aiarea;
import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Favorite;
import com.ruoyi.system.domain.Guide;
import com.ruoyi.system.domain.vo.BookVo;
import com.ruoyi.system.mapper.AiareaMapper;
import com.ruoyi.system.mapper.GuideMapper;
import com.ruoyi.system.service.FavoriteService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: FavoriteController
 * Package: com.ruoyi.system.controller
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 23:03
 * @Version 1.0
 */

@RestController
@RequestMapping("/favorite/")
@Schema(description = "导航数据对象")
@Anonymous
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private GuideMapper guideMapper;

    @Autowired
    private AiareaMapper aiareaMapper;

    @PostMapping("addFavorite")
    public String addFavorite(
            @RequestBody
            Favorite  favorite
    ){
        if(favoriteService.addFavorite(favorite)!="返回值有问题"){
            return "success";
        }
        return "fail";
    }

    @DeleteMapping("deleteFavorite")
    public String deleteFavorite(@RequestParam Integer id){
        favoriteService.deleteFavorite(id);
        return "success";
    }

    @GetMapping("getFavoritesBook")
    public List<BookVo> getFavoritesBook(Integer userId){
        return favoriteService.getFavoritesBook(userId);
    }
    @GetMapping("getFavoritesGuide")
    public List<Guide> getFavoritesGuide(Integer userId){
        return favoriteService.getFavoritesGuide(userId);
    }

    @DeleteMapping("deleteGuide")
    public String deleteGuide(Integer id){
        favoriteService.deleteGuide(id);
        return "success";
    }

    @GetMapping("getGuide")
    public List<Guide> getGuide(Integer userId){
        return guideMapper.getGuide(userId);
    }

    @GetMapping("getAiAreaBook")
    public List<Aiarea> getAiAreaBook(Integer id){
        return guideMapper.getAiAreaBook(id);
    }

    @DeleteMapping("deleteAiarea")
    public String deleteAiarea(Integer id){
        aiareaMapper.deleteAiarea(id);
        return "success";
    }
}
