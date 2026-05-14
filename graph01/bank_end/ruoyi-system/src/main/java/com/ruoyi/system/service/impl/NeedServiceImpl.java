package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.Need;
import com.ruoyi.system.mapper.NeedMapper;
import com.ruoyi.system.service.NeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: NeedServiceImpl
 * Package: com.ruoyi.system.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 21:49
 * @Version 1.0
 */
@Service
public class NeedServiceImpl implements NeedService {
    @Autowired
    private NeedMapper needMapper;
    @Override
    public List<Need> getNeeds(Integer userId) {
        return needMapper.selectNeedsByUserId(userId);
    }

    @Override
    public void addNeed(Need need) {
        needMapper.insert(need);
    }

    @Override
    public void deleteNeed(Integer id) {
        needMapper.deleteById(id);
    }

    @Override
    public void updateNeed(Need need) {
        needMapper.updateById(need);
    }
}
