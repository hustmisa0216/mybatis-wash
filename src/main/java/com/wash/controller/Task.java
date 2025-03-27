package com.wash.controller;

import com.wash.service.Recorder;
import com.wash.service.Selecter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/3/25
 * @Description
 */
@EnableScheduling
@Component// 启用定时任务支持
public class Task {
    @Autowired
    private Selecter selecter;

    @Autowired
    private Recorder recorder;

        // ... existing code ...

        // 添加定时任务方法
        @Scheduled(cron = "0 12 0 * * ?")  // 每天凌晨00:10执行
        public void scheduledTask() throws IOException {
            String all="3323\n" +
                    "3191\n" +
                    "3291\n" +
                    "3190\n" +
                    "3066\n" +
                    "3361\n" +
                    "3033\n" +
                    "3265\n" +
                    "3044\n" +
                    "3362\n" +
                    "3351\n" +
                    "3325\n" +
                    "3300\n" +
                    "3215\n" +
                    "3434\n" +
                    "3122\n" +
                    "3453\n" +
                    "3205\n" +
                    "3083\n" +
                    "3422\n" +
                    "3250\n" +
                    "9\n" +
                    "3073\n" +
                    "3089\n" +
                    "3104\n" +
                    "3392\n" +
                    "3230\n" +
                    "3054\n" +
                    "3266\n" +
                    "3310\n" +
                    "3353\n" +
                    "3411\n" +
                    "3221\n" +
                    "3281\n" +
                    "3280\n" +
                    "3243\n" +
                    "3248\n" +
                    "3333\n" +
                    "3092\n" +
                    "3289\n" +
                    "3308\n" +
                    "3231\n" +
                    "3260\n" +
                    "3177\n" +
                    "3258\n" +
                    "3433\n" +
                    "3297\n" +
                    "3117\n" +
                    "3234\n" +
                    "3160\n" +
                    "3091\n" +
                    "3114\n" +
                    "3194\n" +
                    "3455\n" +
                    "3393\n" +
                    "3343\n" +
                    "3128\n" +
                    "3026\n" +
                    "3024\n" +
                    "3184\n" +
                    "3224\n" +
                    "3283\n" +
                    "3212\n" +
                    "3278\n" +
                    "3166\n" +
                    "3014\n" +
                    "3043\n" +
                    "3188\n" +
                    "3372\n" +
                    "3437\n" +
                    "3382\n" +
                    "3364\n" +
                    "3225\n" +
                    "3196\n" +
                    "3223\n" +
                    "3220\n" +
                    "8\n" +
                    "3432\n" +
                    "3229\n" +
                    "3021\n" +
                    "3240\n" +
                    "3193\n" +
                    "3010\n" +
                    "3203\n" +
                    "3245\n" +
                    "3450\n" +
                    "16\n" +
                    "3342\n" +
                    "3311\n" +
                    "3279\n" +
                    "3329\n" +
                    "3413\n" +
                    "3107\n" +
                    "3259\n" +
                    "3348\n" +
                    "3216\n" +
                    "3403\n" +
                    "3296\n" +
                    "3202\n" +
                    "3312\n" +
                    "3421\n" +
                    "3161\n" +
                    "3374\n" +
                    "3321\n" +
                    "3100\n" +
                    "3180\n" +
                    "3390\n" +
                    "3157\n" +
                    "3315\n" +
                    "3252\n" +
                    "3059\n" +
                    "3153\n" +
                    "3340\n" +
                    "3062\n" +
                    "3069\n" +
                    "3322\n" +
                    "3156\n" +
                    "3206\n" +
                    "3356\n" +
                    "3320\n" +
                    "3261\n" +
                    "3095\n" +
                    "3213\n" +
                    "3106\n" +
                    "3168\n" +
                    "3035\n" +
                    "4\n" +
                    "3174\n" +
                    "3357\n" +
                    "3111\n" +
                    "3121\n" +
                    "3012\n" +
                    "3436\n" +
                    "3045\n" +
                    "3371\n" +
                    "3440\n" +
                    "3116\n" +
                    "3120\n" +
                    "3274\n" +
                    "3313\n" +
                    "3441\n" +
                    "3414\n" +
                    "3405\n" +
                    "3377\n" +
                    "3130\n" +
                    "3285\n" +
                    "3381\n" +
                    "3384\n" +
                    "3385\n" +
                    "3150\n" +
                    "3334\n" +
                    "3388\n" +
                    "3178\n" +
                    "3031\n" +
                    "3075\n" +
                    "3053\n" +
                    "3182\n" +
                    "3288\n" +
                    "3034\n" +
                    "3080\n" +
                    "3152\n" +
                    "3039\n" +
                    "3123\n" +
                    "3113\n" +
                    "3379\n" +
                    "3027\n" +
                    "3301\n" +
                    "3396\n" +
                    "3009\n" +
                    "3051\n" +
                    "3132\n" +
                    "3287\n" +
                    "3309\n" +
                    "3076\n" +
                    "3367\n" +
                    "3151\n" +
                    "3218\n" +
                    "3173\n" +
                    "3204\n" +
                    "3232\n" +
                    "3439\n" +
                    "3124\n" +
                    "3176";

            AtomicInteger allcome=new AtomicInteger(0);
            String v[]=all.split("\n");
            for(String s:v){
                try{
                    String tt=selecter.select(Integer.valueOf(s), null, null, null,allcome);
                    System.out.println("res:"+tt+"\n");
                }catch (Exception e){
                    System.out.println(ExceptionUtils.getStackTrace(e));
                }
            }
            recorder.scheduleRecord("all",allcome.get());
        }


    }



