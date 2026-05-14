package com.atguigu.ssyx.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.exception.SsyxException;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.common.utils.JwtHelper;
import com.atguigu.ssyx.enums.UserType;
import com.atguigu.ssyx.model.user.User;
import com.atguigu.ssyx.user.service.UserService;
import com.atguigu.ssyx.user.utils.ConstantPropertiesUtil;
import com.atguigu.ssyx.user.utils.HttpClientUtils;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import com.atguigu.ssyx.vo.user.UserLoginVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: WeixinApiController
 * Package: com.atguigu.ssyx.user.controller
 * Description:
 *
 */
@RequestMapping("/api/user/weixin")
@RestController
//@RequestMapping("/user/weixin")
public class WeixinApiController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "微信登录获取openid(小程序)")
    @GetMapping("wxLogin/{code}")
    public Result callback(@PathVariable String code) {

        //1.获取授权临时票据也就是code码

        //2.通过code码，appid和appscrect换取access_token
        ////拿小程序id
        String wxOpenAppId = ConstantPropertiesUtil.WX_OPEN_APP_ID;
        ////拿小程序密钥
        String wxOpenAppSecret = ConstantPropertiesUtil.WX_OPEN_APP_SECRET;

        //发送get请求，然后进行拼接
        StringBuffer sb = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/jscode2session")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&js_code=%s")
                .append("&grant_type=authorization_code");

        String tokenUrl = String.format(
                sb.toString(),
                wxOpenAppId,
                wxOpenAppSecret,
                code);
        //3.根据access_token获取微信用户基本信息
        String result = null;
        try {
            result = HttpClientUtils.get(tokenUrl);
        } catch (Exception e) {
            throw new SsyxException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        //4.现根据openid进行数据库查询，如果是第一次请求存入数据库，如果不是就不存
        JSONObject jsonObject = JSONObject.parseObject(result);
        String key = jsonObject.getString("session_key");
        String openid = jsonObject.getString("openid");

        User user = userService.getSelectOneById(openid);
        if(user == null){
            user = new User();
            user.setOpenId(openid);
            user.setNickName(openid);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            userService.save(user);
        }
        //5.根据userId查询提货点和团长信息
        LeaderAddressVo leaderAddressVo = userService.getAddressLeaderByUserId(user.getId());
        //数据返回，发送到redis
        UserLoginVo userLoginVo = this.userService.getUserLoginVo(user.getId());

        redisTemplate.opsForValue().set(
                RedisConst.USER_LOGIN_KEY_PREFIX + user.getId(),
                 userLoginVo,
                 RedisConst.USERKEY_TIMEOUT,
                 TimeUnit.DAYS);

        Map<String,Object> map = new HashMap<>();
        String name = user.getNickName();
        map.put("user", user);
        map.put("leaderAddressVo", leaderAddressVo);
        String token = JwtHelper.createToken(user.getId(), name);
        map.put("token", token);

        return Result.ok(map);
    }

    @PostMapping("/auth/updateUser")
    @ApiOperation(value = "更新用户昵称与头像")
    public Result updateUser(@RequestBody User user) {
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]", "*"));
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok(null);
    }
}
