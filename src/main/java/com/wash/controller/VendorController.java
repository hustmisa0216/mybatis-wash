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
        // 1. vendor必选 site不选就是全部
        // 2. date未输入->给dec就按dec*9找一天,不给就按今天收入选一天
        // 3. date输入->给dec就按dec筛选,不给dec就按给定日期的1/9

        // 示例：打印接收到的参数
        String result = String.format("Vendor ID: %d, Dec Amount: %d, Site ID: %d, Date: %d",
                inputVendorId, inputDecAmount, inputSiteId, inputDate);

        if (inputVendorId == null || (inputDecAmount != null && inputDecAmount.intValue() > 30000)) {
            return ResponseEntity.ok("请确认输入参数：" + result);
        }

        try {
           return ResponseEntity.ok(selecter.select(inputVendorId, inputSiteId, inputDate, inputDecAmount));
        } catch (Throwable e) {
            throw new RuntimeException(String.valueOf(e.getStackTrace()));
        }
        // 返回结果
    }
}