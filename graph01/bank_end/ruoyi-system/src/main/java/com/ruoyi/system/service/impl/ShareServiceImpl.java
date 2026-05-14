package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.IdEncryptUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Favorite;
import com.ruoyi.system.mapper.BookMapper;
import com.ruoyi.system.mapper.FavoriteMapper;
import com.ruoyi.system.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ClassName: ShareServiceImpl
 * Package: com.ruoyi.system.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 21:02
 * @Version 1.0
 */
@Service
public class ShareServiceImpl implements ShareService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public String getEncryptedShare(Integer id) {
        Book book = bookMapper.getOne(id);
        if (book == null) {
            return null;
        }
        return IdEncryptUtils.encrypt(id);
    }

    @Override
    public String getShare(String share,Integer id) {
        Integer decryptId = IdEncryptUtils.decrypt(share);
        favoriteMapper.addFavorite(
                new Favorite(null, decryptId, 0, id)
        );
        return "success";
    }
}
