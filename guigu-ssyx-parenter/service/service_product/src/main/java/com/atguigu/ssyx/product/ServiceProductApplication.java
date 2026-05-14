package com.atguigu.ssyx.product;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
/**
 * ClassName: ServiceProductApplication
 * Package: com.atguigu.ssyx.product
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/22 14:10
 * @Version 1.0
 */
@SpringBootApplication(exclude = {
        RedisReactiveHealthContributorAutoConfiguration.class,
        RedissonAutoConfiguration.class,
        MongoAutoConfiguration.class
})
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.atguigu.ssyx.mq.service" // RabbitService 所在的包路径
})
@MapperScan("com.atguigu.ssyx.product.mapper")// 添加这行
public class ServiceProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class, args);
    }
}
