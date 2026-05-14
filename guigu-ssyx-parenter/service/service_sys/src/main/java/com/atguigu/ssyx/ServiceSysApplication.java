package com.atguigu.ssyx;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ClassName: ServiceSysApplication
 * Package: com.atguigu.ssyx
 * Description:
 *

 */
@SpringBootApplication(exclude = {
        RedisReactiveHealthContributorAutoConfiguration.class,
        RedissonAutoConfiguration.class,
        MongoAutoConfiguration.class
})
@MapperScan("com.atguigu.ssyx.sys.mapper")
@EnableDiscoveryClient
public class ServiceSysApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSysApplication.class, args);
    }
}
