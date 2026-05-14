package com.ruoyi.system.service;

/**
 * ClassName: ShareService
 * Package: com.ruoyi.system.service
 * Description:
 *
 * @Author 张鹏
 * @Create 2026/4/4 21:02
 * @Version 1.0
 */

public interface ShareService {

    String getEncryptedShare(Integer id);

    String getShare(String share, Integer id);
}
