package com.llmeval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.llmeval.mapper")
public class LlmEvalApplication {
    public static void main(String[] args) {
        SpringApplication.run(LlmEvalApplication.class, args);
    }
} 