package com.ruoyi.system.service;

import com.ruoyi.system.domain.NavigationData;

import java.util.Map;

/**
 * ClassName: StartNavigation
 * Package: com.ruoyi.system.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/9/12 09:32
 * @Version 1.0
 */
public interface StartNavigationService {
    Map<String, Long> getTextCloud(
            NavigationData navigationData,
            NavigationData navigationData1,
            String bookType,
            String WordsType,
            String byWay,
            Integer userId
    );

    String getText(
            NavigationData origin,
            NavigationData destination,
            String  bookType, String wordsType
    );

    String getChapter(
            Long bookId,
            Integer trajectoryId,
            Integer length
    );

    Map<String, String> getArea(String longitude, String latitude, Integer userId ,String cacheKey, String content);

    Map<String, String> getPoi(String longitude, String latitude, Integer userId ,String cacheKey,String content);

    Long getTextCloudNoBook(
            NavigationData navigationData,
            NavigationData navigationData1,
            String byWay
    );


    String isNavigation(Integer userId, Boolean isNavigation);

    String deleteRedis(Integer userId);
}
