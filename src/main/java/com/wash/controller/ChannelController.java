package com.wash.controller;

import com.wash.service.Selecter;
import com.wash.service.channel.Start;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChannelController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private Start start;

    @GetMapping("channel/dec")
    public ResponseEntity<String> dec(
            @RequestParam(value = "channelId",required = true) Integer channelId,
            @RequestParam(value = "startDate",required = false) Integer startDate,
            @RequestParam(value = "endDate",required = false) Integer endDate){

        // 这里可以添加你的业务逻辑，例如：
        // 1. vendor必选 site不选就是全部
        // 2. date未输入->给dec就按dec*9找一天,不给就按今天收入选一天
        // 3. date输入->给dec就按dec筛选,不给dec就按给定日期的1/9


        try {
           return ResponseEntity.ok(start.start(channelId,startDate,endDate));
        } catch (Throwable e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return ResponseEntity.ok(e.getMessage());
        }
        // 返回结果
    }
}