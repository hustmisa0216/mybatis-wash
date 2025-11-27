package com.soccer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soccer.OkoooHttpClient;
import com.soccer.service.OkoooService;

/**
 * Okooo网站数据获取控制器
 */
@RestController
@RequestMapping("/okooo")
public class OkoooController {

    private final OkoooHttpClient okoooHttpClient;
    
    private final OkoooService okoooService;

    @Autowired
    public OkoooController(OkoooHttpClient okoooHttpClient, OkoooService okoooService) {
        this.okoooHttpClient = okoooHttpClient;
        this.okoooService = okoooService;
    }

    /**
     * 获取指定日期的竞彩数据（原始数据）
     * @param date 日期，格式为yyyy-MM-dd，默认值为2016-01-02
     * @return 原始网页内容
     */
    @GetMapping("/jingcai/raw")
    public ResponseEntity<String>  getRawJingCaiData(
            @RequestParam(value = "date", defaultValue = "2016-01-02") String date) {
        try {
            String content = okoooHttpClient.getJingCaiData(date);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取原始数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定日期的竞彩数据并进行处理（通过service）
     * @param date 日期，格式为yyyy-MM-dd，默认值为2016-01-02
     * @return 处理后的竞彩数据
     */
    @GetMapping("/jingcai")
    public ResponseEntity<String>  start(
            @RequestParam(value = "date", defaultValue = "2016-01-02") String date) {
        try {
            // 通过service获取并处理数据
            String processedContent = okoooService.getProcessedJingCaiData(date);
            return ResponseEntity.ok(processedContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取并处理数据失败: " + e.getMessage());
        }
    }
}