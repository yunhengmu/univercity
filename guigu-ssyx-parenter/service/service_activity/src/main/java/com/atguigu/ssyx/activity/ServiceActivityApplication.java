package com.atguigu.ssyx.activity;

import org.apache.http.util.Args;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ClassName: ServiceActivityApplication
 * Package: com.atguigu.ssyx.activity
 * Description:
 *
 */

@SpringBootApplication(exclude = {
        RedisReactiveHealthContributorAutoConfiguration.class,
        RedissonAutoConfiguration.class,
        MongoAutoConfiguration.class
})
@MapperScan("com.atguigu.ssyx.activity.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.ssyx.client.product")
public class ServiceActivityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceActivityApplication.class, args);
    }
}
