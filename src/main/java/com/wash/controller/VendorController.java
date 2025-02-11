package com.wash.controller;

import com.wash.service.Selecter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VendorController {

    @Autowired
    private Selecter selecter;

    @GetMapping("/dec")
    public ResponseEntity<String> dec(
            @RequestParam(value = "inputVendorId",required = true) Integer inputVendorId,
            @RequestParam(value = "inputDecAmount",required = false) Integer inputDecAmount,
            @RequestParam(value = "inputSiteId",required = false) Integer inputSiteId,
            @RequestParam(value = "inputDate", required = false) Integer inputDate) {

        // 这里可以添加你的业务逻辑，例如：
        // 1. 验证输入参数
        // 2. 调用服务层方法进行处理
        // 3. 返回处理结果

        // 示例：打印接收到的参数
        String result = String.format("Vendor ID: %d, Dec Amount: %d, Site ID: %d, Date: %d",
                inputVendorId, inputDecAmount, inputSiteId, inputDate);

        if (inputVendorId == null || (inputDecAmount != null && inputDecAmount.intValue() > 30000)) {
            return ResponseEntity.ok("请确认输入参数：" + result);
        }

        try {
            selecter.select(inputVendorId, inputSiteId, inputDate, inputDecAmount);
        } catch (Throwable e) {
            throw new RuntimeException(String.valueOf(e.getStackTrace()));
        }
        // 返回结果
        return ResponseEntity.ok(result);
    }
}