package com.goblin.aicodeuser;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.goblin.aicodeuser.mapper")
@ComponentScan("com.goblin")
@EnableDubbo
public class AiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiCodeUserApplication.class, args);
    }
}