package com.atguigu.ssyx.product.controller;

import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.product.service.FileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * ClassName: FileUploadController
 * Package: com.atguigu.ssyx.product.controller
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/28 09:56
 * @Version 1.0
 */
@Api(tags = "文件上传接口")
@RestController
@RequestMapping("admin/product/")
@CrossOrigin
public class FileUploadController {
    @Autowired
    private FileUploadService fileUploadService;

    @ApiOperation("图片上传")
    @PostMapping( "fileUpload")
    public Result fileUpload(@RequestPart MultipartFile file) throws Exception{
        return Result.ok(fileUploadService.fileUpload(file));
    }
}
