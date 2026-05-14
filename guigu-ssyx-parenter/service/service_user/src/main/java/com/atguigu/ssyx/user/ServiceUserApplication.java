package com.atguigu.ssyx.user;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
* ClassName: ServiceUserApplication
* Package: com.atguigu.ssyx.user 
* Description:

*/
@SpringBootApplication(
        exclude = {
//           RedissonAutoConfiguration.class,
                MongoAutoConfiguration.class,
                RedissonAutoConfiguration.class
        }
)
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.atguigu.ssyx.user.mapper")
@ComponentScan(basePackages = {"com.atguigu.ssyx.common.auth",
        "com.atguigu.ssyx.common.config","com.atguigu.ssyx.user"})
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class, args);
    }
}