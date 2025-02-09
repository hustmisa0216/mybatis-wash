package com.wash;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wash.mapper") // 确保此包路径正确

public class CrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudApplication.class, args);

    }
}

