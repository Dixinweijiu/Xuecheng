package com.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author jxchen
 * @version 1.0
 * @description 内容管理服务启动类
 * @date 2023/9/7 10 52
 */

@SpringBootApplication
@EnableFeignClients(basePackages={"com.xuecheng.content.feignClient"})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
