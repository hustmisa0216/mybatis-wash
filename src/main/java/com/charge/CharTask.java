package com.charge;

import com.charge.entity.ChargeTaskRecord;
import com.charge.service.CharRecorder;
import com.charge.service.ChargeSelector;
import com.wash.config.AllConfig;
import com.wash.entity.TaskRecord;
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
public class CharTask {
    @Autowired
    private ChargeSelector chargeSelector;

    @Autowired
    private CharRecorder charRecorder;

        // ... existing code ...

        // 添加定时任务方法
        @Scheduled(cron = "0 18 0 * * ?")  // 每天凌晨00:10执行
        public void scheduledTask() throws IOException {
            List<String> v= Arrays.asList(AllConfig.charString.split("\n"));
            Collections.shuffle(v);
            Map<Integer, ChargeTaskRecord> map=new HashMap<>();
            for(String s:v){

                int ven=Integer.parseInt(s);
                ChargeTaskRecord chargeTaskRecord=map.computeIfAbsent(ven, k->new ChargeTaskRecord(ven));
                try{
                    String tt=chargeSelector.select(ven, null,chargeTaskRecord);
                    System.out.println("res:"+tt+"\n");
                }catch (Exception e){
                    System.out.println(ExceptionUtils.getStackTrace(e));
                }
            }
            charRecorder.scheduleRecord(map);
        }


    public static void main(String[] args) throws IOException {
            new CharTask().scheduledTask();
    }
}

