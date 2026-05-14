package com.atguigu.ssyx.product.service.impl;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.atguigu.ssyx.product.service.FileUploadService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

/**
 * ClassName: FileUploadServiceImpl
 * Package: com.atguigu.ssyx.product.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/28 09:58
 * @Version 1.0
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {
    @Value("${aliyun.endpoint}")
    private String endpoint;

    @Value("${aliyun.bucketname}")
    private String bucketname;

    @Value("${aliyun.keyid}")
    private String keyid;

    @Value("${aliyun.keysecret}")
    private String keysecret;

    @Override
    public String fileUpload(MultipartFile file) throws Exception {
        try {
            // 创建OSSClient实例。
            // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
            OSS ossClient = new  OSSClientBuilder()
                    .build(endpoint,keyid,keysecret);
            //1.得到文件输入流
            InputStream inputStream = file.getInputStream();
            //2.获取文件实际名称
            String objectName = file.getOriginalFilename();

            //唯一化处理，获取实际名称
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            objectName = uuid+objectName;

            //根据年月日进行分组化处理
            String currentDateTime = new DateTime().toString("yyyy/MM/dd");
            objectName = currentDateTime+"/"+objectName;

            //第三个 文件输入流
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketname, objectName, inputStream);
            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            putObjectRequest.setProcess("true");
            // 上传文件,上传成功返回200。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            System.out.println(result.getResponse().getStatusCode());
            System.out.println(result.getResponse().getErrorResponseAsString());
            System.out.println(result.getResponse().getUri());
            ossClient.shutdown();

            return result.getResponse().getUri();
        } catch (Exception ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }
        return null;
    }
}
