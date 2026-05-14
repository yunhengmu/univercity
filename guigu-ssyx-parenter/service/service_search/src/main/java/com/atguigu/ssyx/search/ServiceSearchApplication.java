package com.atguigu.ssyx.search;

import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * ClassName: ServiceSearchApplication
 * Package: com.atguigu.ssyx.search
 * Description:
 *

 */
@SpringBootApplication(exclude = {
        RedisReactiveHealthContributorAutoConfiguration.class,
        RedissonAutoConfiguration.class,
        MongoAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "com.atguigu.ssyx.client.product",
        "com.atguigu.ssyx.client.activity",
        "com.atguigu.ssyx.client.search"
})
@ComponentScan(basePackages = {"com.atguigu.ssyx.common.config","com.atguigu.ssyx.search"})
//@EnableFeignClients(basePackages = "com.atguigu.ssyx.client.product")
public class ServiceSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSearchApplication.class, args);
    }

    //使用的是Jackson库中的Jackson2JsonMessageConverter类，代替使用jdk自带的序列化
    @Bean
    public MessageConverter jacksonMessageConvertor(){
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setCreateMessageIds(true);//开启消息id的自动生成功能
        return jackson2JsonMessageConverter;
    }
}
