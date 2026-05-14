package com.ruoyi.system.service;

import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.domain.Customer;
import com.ruoyi.system.domain.vo.CustomerVo;

import java.util.List;

/**
 * ClassName: UserDataService
 * Package: com.ruoyi.system.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/2/1 11:11
 * @Version 1.0
 */
public interface UserDataService {
    Customer getUser(Integer userId);

    int updateUser(CustomerVo customer);

    List<Book> getBookByTags(Integer userId, String tags);

    String deleteBook(Integer bookId);

    List<Book> getBooks(Integer userId, Integer pageNum, Integer pageSize);

    List<Chapter> getChapters(Integer bookId, Integer pageNum, Integer pageSize);
}
