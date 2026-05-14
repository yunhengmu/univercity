package com.atguigu.ssyx.user.service;

import com.atguigu.ssyx.model.user.User;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import com.atguigu.ssyx.vo.user.UserLoginVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

/**
 * ClassName: UserService
 * Package: com.atguigu.ssyx.user.service
 * Description:
 *

 */
@Service
public interface UserService extends IService<User> {
    User getSelectOneById(String openid);

    LeaderAddressVo getAddressLeaderByUserId(Long id);

    UserLoginVo getUserLoginVo(Long id);
}
