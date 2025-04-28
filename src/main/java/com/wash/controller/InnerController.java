package com.wash.controller;

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
@RequestMapping("/inner") // 设置基础路径
public class InnerController {

    @Autowired
    private CreatePay createPay;

    @GetMapping("/dec")
    public ResponseEntity<String> create(
            @RequestParam(value = "siteId",required = true) Integer siteId,
            @RequestParam(value = "num", required = true) Integer num) {


        createPay.create(siteId,num);
        return ResponseEntity.ok("ok");

    }


}
