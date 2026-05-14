package com.atguigu.ssyx.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
		RedisReactiveHealthContributorAutoConfiguration.class,
		RedissonAutoConfiguration.class,
		MongoAutoConfiguration.class,
		DataSourceAutoConfiguration.class
})//取消数据源自动配置
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
		"com.atguigu.ssyx.client.product",
		"com.atguigu.ssyx.client.activity"})
//		,"com.atguigu.ssyx.client.search"
//		,"com.atguigu.ssyx.client.user"
//		,"com.atguigu.ssyx.client.activity"})

@ComponentScan(basePackages = {
		"com.atguigu.ssyx.common.auth",
		"com.atguigu.ssyx.cart",
		"com.atguigu.ssyx.common.config"})
public class ServiceCartApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceCartApplication.class, args);
	}

}