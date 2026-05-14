package com.ruoyi.system.service.impl;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.Book;
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.domain.Customer;
import com.ruoyi.system.domain.vo.CustomerVo;
import com.ruoyi.system.mapper.CustomerMapper;
import com.ruoyi.system.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * ClassName: UserDataServiceImpl
 * Package: com.ruoyi.system.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/2/1 11:11
 * @Version 1.0
 */
@Service
public class UserDataServiceImpl implements UserDataService {
    @Autowired
    private CustomerMapper customerMapper;
    @Override
    public Customer getUser(Integer userId) {
        return customerMapper.getUserId(userId);
    }

    @Override
    public int updateUser(CustomerVo customer) {
        Customer customer1 = customerMapper.getUserId(customer.getId());


        Customer build = Customer.builder()
                .id(customer.getId())
                .name(customer.getNickName())
                .description(customer.getIntroduction())
                .imageUrl(customer.getImageUrl())
                .status(customer1.getStatus())
                .createdAt(DateUtils.dateTime())
                .updatedAt(customer1.getUpdatedAt()).build();
        return customerMapper.upUser(build);
    }

    @Override
    public List<Book> getBookByTags(Integer userId, String tags) {
        return customerMapper.getBookByTags(userId, tags);
    }

    @Override
    public String deleteBook(Integer bookId) {
        customerMapper.deleteBook(bookId);
        return "成功";
    }

    @Override
    public List<Book> getBooks(Integer userId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return customerMapper.getBookByUserId(userId, offset, pageSize);
    }

    @Override
    public List<Chapter> getChapters(Integer bookId, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return customerMapper.getChapters(bookId,offset, pageSize);
    }

}
