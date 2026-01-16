package com.example.aichat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 标注为 Spring Boot 应用程序的入口
@MapperScan("com.example.aichat.mapper") // 添加此注解以扫描Mapper接口
public class AiChatApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用程序
        SpringApplication.run(AiChatApplication.class, args);
    }
}
