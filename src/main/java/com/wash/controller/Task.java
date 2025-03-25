package com.wash.controller;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/3/25
 * @Description
 */
@EnableScheduling  // 启用定时任务支持
public class Task {

        // ... existing code ...

        // 添加定时任务方法
        @Scheduled(cron = "0 10 0 * * ?")  // 每天凌晨00:10执行
        public void scheduledTask() throws IOException {
            String all="3323\n" +
                    // ... existing string data ...
                    "3009\n" +
                    "3051\n" +
                    "3132\n" +
                    "3287\n" +
                    "3396\n" +
                    "3076\n" +
                    "3309";

            String v[]=all.split("\n");

            for(String s:v){
                System.out.println(s);
            }
        }


    }


}
