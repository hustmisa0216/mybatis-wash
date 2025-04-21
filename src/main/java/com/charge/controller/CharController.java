package com.charge.controller;

import com.charge.entity.ChargeTaskRecord;
import com.charge.service.ChargeSelector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/3/5
 * @Description
 */
@RestController
@RequestMapping("/char") // 设置基础路径
public class CharController {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.wash.controller.VendorController.class);

    @Autowired
    private ChargeSelector chargeSelector;

    @PostConstruct
    public void start(){
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    @GetMapping("/dec")
    public ResponseEntity<String> dec(@RequestParam(value = "inputVendorId", required = true) Integer inputVendorId,
                                      @RequestParam(value = "amount", required = false)Integer amount) {

        if(amount!=null&&amount>180){
            return ResponseEntity.ok("金额不能大于180");
        }
        try {
            return ResponseEntity.ok(chargeSelector.select(inputVendorId,amount, new ChargeTaskRecord(inputVendorId)));
        } catch (Throwable e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return ResponseEntity.ok(e.getMessage());
        }

    }

}
