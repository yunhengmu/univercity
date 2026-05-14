package com.atguigu.ssyx.product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: FileUploadService
 * Package: com.atguigu.ssyx.product.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/28 09:57
 * @Version 1.0
 */
public interface FileUploadService {
    String fileUpload(MultipartFile file) throws Exception;
}
