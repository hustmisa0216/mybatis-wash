package com.wash.controller;

import com.wash.config.AllConfig;
import com.wash.entity.TaskRecord;
import com.wash.recover.RecoverByOneDay;
import com.wash.service.Recorder;
import com.wash.service.Selecter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/3/25
 * @Description
 */
@EnableScheduling
@Component// 启用定时任务支持
public class VenTask {
    @Autowired
    private Selecter selecter;

    @Autowired
    private RecoverByOneDay recover;
    @Autowired
    private Recorder recorder;

        // ... existing code ...

        // 添加定时任务方法
        @Scheduled(cron = "0 12 0 * * ?")  // 每天凌晨00:10执行
        public void scheduledTask() throws IOException {
            List<String> v= Arrays.asList(AllConfig.vs.split("\n"));
            Collections.shuffle(v);
            Map<Integer, TaskRecord> map=new HashMap<>();
            for(String s:v){

                String ss[]=s.split("\t");
                int ven=Integer.valueOf(ss[0]);
                int com=Integer.valueOf(ss[1]);
                TaskRecord taskRecord=map.computeIfAbsent(ven,k->new TaskRecord(ven));
                try{
                    String tt=selecter.select(taskRecord, com, null, null);
                    System.out.println("res:"+tt+"\n");
                }catch (Exception e){
                    System.out.println(ExceptionUtils.getStackTrace(e));
                }
            }
            recorder.scheduleRecord(map);
        }


    public static void main(String[] args) throws IOException {
            new VenTask().scheduledTask();
    }
}





