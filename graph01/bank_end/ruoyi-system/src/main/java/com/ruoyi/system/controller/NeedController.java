package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.system.domain.Need;
import com.ruoyi.system.service.NeedService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: NeedController
 * Package: com.ruoyi.system.controller
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/3 21:48
 * @Version 1.0
 */
@RestController
@RequestMapping("/need/")
@Schema(description = "导航数据对象")
@Anonymous
public class NeedController {
    @Autowired
    private NeedService needService;

    @GetMapping("getNeeds")
    public List<Need> getNeeds(Integer userId){
        return needService.getNeeds(userId);
    }

    @PostMapping("addNeed")
    public String addNeed(
            @RequestBody
            Need need){
        needService.addNeed(need);
        return "success";
    }

    @DeleteMapping("deleteNeed")
    public String deleteNeed(
            Integer id){
        needService.deleteNeed(id);
        return "success";
    }

    @PostMapping("updateNeed")
    public String updateNeed(
            @RequestBody
            Need need){
        needService.updateNeed(need);
        return "success";
    }
}
