package com.atguigu.ssyx.product.service;

import com.atguigu.ssyx.model.product.Attr;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * ClassName: AttrGroupService
 * Package: com.atguigu.ssyx.product.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/22 14:21
 * @Version 1.0
 */
public interface AttrService extends IService<Attr> {

    List<Attr> selectListById(Long groupId);
}
