package com.ruoyi.system.utils;

import com.ruoyi.system.domain.Area;
import com.ruoyi.system.mapper.AreaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: atArea
 * Package: com.ruoyi.system.utils
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/2/8 14:55
 * @Version 1.0
 */

@Component
public class AtArea {

    @Autowired
    private AreaMapper areaMapper;

    public Area getArea(Integer trajectoryId, Integer length) {
        List<Area> areas = areaMapper.selectByTrajectoryId(trajectoryId);

        if (areas == null || areas.isEmpty()) {
            return null;
        }

        Integer itemTime = 1000;
        Integer systemTime = 1000;
        Integer durationSum = 0;

        for (Area area : areas) {
            if (area.getDuration() == null) {
                area.setDuration("0");
            }

            durationSum += Integer.parseInt(area.getDuration());

            // 当累计时间达到阈值时返回
            if (durationSum >= (length * itemTime) + systemTime) {
                return area;
            }
        }

        // 时间不足时返回最后一个点
        return areas.get(areas.size() - 1);
    }
}
