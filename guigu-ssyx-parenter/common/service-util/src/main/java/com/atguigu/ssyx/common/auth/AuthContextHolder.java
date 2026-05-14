package com.atguigu.ssyx.common.auth;

import com.atguigu.ssyx.vo.user.UserLoginVo;

/**
 * ClassName: AuthContextHolder
 * Package: com.atguigu.ssyx.common.security
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/8/7 08:32
 * @Version 1.0
 */
public class AuthContextHolder {
    //用户id
    private static ThreadLocal<Long> userId = new ThreadLocal<>();
    //仓库id
    private static ThreadLocal<Long> wareId = new ThreadLocal<>();
    //用户信息对象
    private static ThreadLocal<UserLoginVo>  userLoginVo = new ThreadLocal<>();

    public static void setUserId(Long _userId){
        userId.set(_userId);
    }

    public static Long getUserId(){
        return userId.get();
    }

    public static void setWareId(Long _wareId){
        wareId.set(_wareId);
    }

    public static Long getWareId(){
        return wareId.get();
    }

    public static void setUserLoginVo(UserLoginVo _userLoginVo){
        userLoginVo.set(_userLoginVo);
    }

    public static UserLoginVo getUserLoginVo(){
        return userLoginVo.get();
    }

}
