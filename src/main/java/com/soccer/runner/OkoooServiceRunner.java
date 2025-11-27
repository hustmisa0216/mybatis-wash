package com.soccer.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import com.soccer.service.OkoooService;

/**
 * OkoooService功能测试运行器
 */
@SpringBootApplication
public class OkoooServiceRunner {

    public static void main(String[] args) {
        // 启动Spring Boot应用
        ApplicationContext context = SpringApplication.run(OkoooServiceRunner.class, args);
        
        // 获取OkoooService的实例
        OkoooService okoooService = context.getBean(OkoooService.class);
        
        try {
            // 测试OkoooService的功能
            System.out.println("测试OkoooService开始...");
            String result = okoooService.getProcessedJingCaiData("2016-01-02");
            
            // 打印结果
            System.out.println("测试成功！获取到的处理后数据：");
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("测试失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}