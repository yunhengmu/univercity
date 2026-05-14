package com.ruoyi.system.utils;

/**
 * ClassName: GainRedisUtil
 * Package: com.ruoyi.system.utils
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 11:52
 * @Version 1.0
 */
public class GainRedisUtil {
    public static String getOneByUserID(Integer userID) {
        return "user:" + userID;
    }
}
