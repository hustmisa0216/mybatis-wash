package com.wash.controller;

import com.alibaba.fastjson.JSON;
import com.wash.service.inner.CreatePay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/4/27
 * @Description
 */
@RestController
@RequestMapping("/create") // 设置基础路径
public class InnerController {

    @Autowired
    private CreatePay createPay;
    @GetMapping("/create")
    public ResponseEntity<String> create(
            @RequestParam(value = "siteId",required = true) Integer siteId,
            @RequestParam(value = "num", required = true) Integer num,
            @RequestParam(value = "begin", required = true) Integer begin,
            @RequestParam(value = "end", required = true) Integer end) {

        String v= JSON.toJSONString(createPay.create(siteId,num,begin,end));
        return ResponseEntity.ok(v);

    }


}
