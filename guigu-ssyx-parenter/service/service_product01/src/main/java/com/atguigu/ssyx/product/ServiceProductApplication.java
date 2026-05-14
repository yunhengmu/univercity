package com.atguigu.ssyx.product;


import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
/**
 * ClassName: ServiceProductApplication
 * Package: com.atguigu.ssyx.product
 * Description:
 *

 */
@SpringBootApplication(exclude = {
        RedisReactiveHealthContributorAutoConfiguration.class,
        RedissonAutoConfiguration.class,
        MongoAutoConfiguration.class
})
@EnableDiscoveryClient
//@ComponentScan(basePackages = {
//        "com.atguigu.ssyx.mq.service" // RabbitService 所在的包路径
//})
@ComponentScan(basePackages = { "com.atguigu.ssyx.common.auth",
        "com.atguigu.ssyx.product",
        "com.atguigu.ssyx.common.config"})
@MapperScan("com.atguigu.ssyx.product.mapper")// 添加这行
public class ServiceProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class, args);
    }
    //使用的是Jackson库中的Jackson2JsonMessageConverter类，代替使用jdk自带的序列化
    @Bean
    public MessageConverter jacksonMessageConvertor(){
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setCreateMessageIds(true);//开启消息id的自动生成功能
        return jackson2JsonMessageConverter;
    }

}
