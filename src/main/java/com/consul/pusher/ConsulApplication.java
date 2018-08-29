package com.consul.pusher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by dapeng
 * Date: 2018/8/28
 * Time: 18:03
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling // 启用定时调度功能，consul需要使用此功能来监控配置改变
public class ConsulApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsulApplication.class);
    }
}
