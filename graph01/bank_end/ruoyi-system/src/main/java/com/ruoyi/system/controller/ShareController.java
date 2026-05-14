package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.system.service.ShareService;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: ShareController
 * Package: com.ruoyi.system.controller
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 20:59
 * @Version 1.0
 */
@RestController
@RequestMapping("/share/")
@Schema(description = "分享")
@Anonymous
public class ShareController {
    @Autowired
    private ShareService shareService;

    @GetMapping("getShare")
    public String getShare(@RequestParam Integer id){
        return shareService.getEncryptedShare(id);
    }

    @PostMapping("postShare")
    public String postShare(@RequestParam String share,@RequestParam Integer id){
        return shareService.getShare(share, id);
    }
}
