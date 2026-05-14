package com.ruoyi.system.service;

import com.ruoyi.system.domain.Need;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: NeedService
 * Package: com.ruoyi.system.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 21:48
 * @Version 1.0
 */

public interface NeedService {

    List<Need> getNeeds(Integer userId);

    void addNeed(Need need);

    void deleteNeed(Integer id);

    void updateNeed(Need need);
}
