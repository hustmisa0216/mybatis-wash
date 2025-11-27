package com.soccer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于测试编码问题的控制器
 */
@RestController
@RequestMapping("/test")
public class EncodingTestController {

    private final RestTemplate restTemplate;

    @Autowired
    public EncodingTestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 测试获取澳客网站数据并返回编码信息
     * @param date 日期，格式为yyyy-MM-dd
     * @return 包含原始内容和编码信息的响应
     */
    @GetMapping("/encoding")
    public ResponseEntity<Map<String, Object>> testEncoding(
            @RequestParam(value = "date", defaultValue = "2016-01-02") String date) {
        try {
            String url = "https://www.okooo.cn/jingcai/" + date + "/";
            
            // 创建HTTP头，与OkoooHttpClient保持一致
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            headers.add("Accept-Encoding", "gzip, deflate, br");
            headers.add("Accept-Language", "zh-CN,zh;q=0.9");
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.97 Safari/537.36 SE 2.X MetaSr 1.0");
            headers.add("Cookie", "Hm_lvt_213d524a1d07274f17dfa17b79db318f=1758266782; HMACCOUNT=80FDC7871CDA93C6; _ga=GA1.1.2086009437.1758266782; PHPSESSID=e0a9ee427e8139271e113191a9af4d27318edac3;");
            
            // 创建HTTP实体
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // 发送GET请求并获取原始响应
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            String content = response.getBody();
            
            // 检查内容中的编码声明
            boolean containsGb2312 = content != null && (content.contains("gb2312") || content.contains("GB2312"));
            boolean containsDoctype = content != null && content.contains("<!DOCTYPE");
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("url", url);
            result.put("statusCode", response.getStatusCodeValue());
            result.put("contentType", response.getHeaders().getContentType());
            result.put("containsGb2312", containsGb2312);
            result.put("containsDoctype", containsDoctype);
            
            // 如果内容存在，返回前1000个字符用于验证
            if (content != null) {
                result.put("contentLength", content.length());
                result.put("sampleContent", content.substring(0, Math.min(1000, content.length())));
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}