package com.soccer.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.soccer.util.HtmlParserUtil;
// 请确保项目中已添加Spring Boot Test依赖，以下为正确导入语句
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
// 请确保项目中已在pom.xml或build.gradle中添加以下依赖：
// Maven:
// <dependency>
//     <groupId>org.springframework.boot</groupId>
//     <artifactId>spring-boot-starter-test</artifactId>
//     <scope>test</scope>
// </dependency>
// Gradle:
// testImplementation 'org.springframework.boot:spring-boot-starter-test'

/**
 * 用于测试Okooo网站数据获取和编码处理的测试类
 */
@SpringBootTest
public class OkoooEncodingTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testEncoding() throws IOException {
        String url = "https://www.okooo.cn/jingcai/2016-01-02/";
        
        // 创建HTTP头，模拟浏览器请求
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        //headers.add("Accept-Encoding", "gzip, deflate, br");
        headers.add("Accept-Language", "zh-CN,zh;q=0.9");
        headers.add("Sec-Ch-Ua", "\"Not)A;Brand\";v=\"24\", \"Chromium\";v=\"116\"");
        headers.add("Sec-Ch-Ua-Mobile", "?0");
        headers.add("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.add("Sec-Fetch-Dest", "document");
        headers.add("Sec-Fetch-Mode", "navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("Sec-Fetch-User", "?1");
        headers.add("Upgrade-Insecure-Requests", "1");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.97 Safari/537.36 SE 2.X MetaSr 1.0");
        
        // 设置Cookie
        headers.add("Cookie", "Hm_lvt_213d524a1d07274f17dfa17b79db318f=1758266782; HMACCOUNT=80FDC7871CDA93C6; _ga=GA1.1.2086009437.1758266782; PHPSESSID=e0a9ee427e8139271e113191a9af4d27318edac3; pm=; LStatus=N; LoginStr=%7B%22welcome%22%3A%22%u60A8%u597D%uFF0C%u6B22%u8FCE%u60A8%22%2C%22login%22%3A%false%2C%22username%22%3A%22%u7FA4%u4EBA%u5934%u53F7%22%2C%22uid%22%3A%223869287%22%2C%22nickname%22%3A%22%u7FA4%u4EBA%u5934%u53F7%22%2C%22type%22%3A%222%22%2C%22memberid%22%3A%223869287%22%2C%22membertype%22%3A%222%22%2C%22vip%22%3A%220%22%2C%22is_dj%22%3A%220%22%2C%22msg%22%3A%22%22%7D; Hm_lpvt_213d524a1d07274f17dfa17b79db318f=1758266782");
        
        // 创建HTTP实体
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            // 发送GET请求
            System.out.println("发送请求到: " + url);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, byte[].class);
            
            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful()) {
                // 获取响应头中的Content-Type
                MediaType contentType = response.getHeaders().getContentType();
                System.out.println("响应Content-Type: " + contentType);
                
                // 获取响应体
                byte[] bodyBytes = response.getBody();
                System.out.println("响应字节长度: " + (bodyBytes != null ? bodyBytes.length : 0));
                
                // 尝试使用不同编码解析响应内容
                if (bodyBytes != null) {
                    // 尝试使用UTF-8
                    String utf8Content = new String(bodyBytes, StandardCharsets.UTF_8);
                    System.out.println("\nUTF-8解码结果前100个字符:");
                    System.out.println(utf8Content.substring(0, Math.min(100, utf8Content.length())));
                    
                    // 尝试使用GB2312
                    String gb2312Content = new String(bodyBytes, "GB2312");
                    System.out.println("\nGB2312解码结果前100个字符:");
                    System.out.println(gb2312Content.substring(0, Math.min(100, gb2312Content.length())));
                    
                    // 检查内容是否包含HTML头部中的encoding声明
                    System.out.println("\nHTML内容中是否包含gb2312: " + gb2312Content.contains("gb2312"));
                    
                    // 如果内容中包含DOCTYPE声明，说明解析成功
                    if (gb2312Content.contains("<!DOCTYPE")) {
                        System.out.println("\nGB2312解码成功，内容包含DOCTYPE声明");
                        
                        // 使用Jsoup解析HTML内容
                        Document document = Jsoup.parse(gb2312Content);
                        
                        // 提取HTML标题
                        String title = document.title();
                        System.out.println("\nHTML标题: " + title);
                        
                        // 提取meta标签中的charset信息
                        Elements metaCharsetElements = document.select("meta[charset]");
                        if (!metaCharsetElements.isEmpty()) {
                            System.out.println("Meta charset: " + metaCharsetElements.first().attr("charset"));
                        }
                        
                        // 提取meta标签中的content-type信息
                        Elements metaContentTypeElements = document.select("meta[http-equiv=Content-Type]");
                        if (!metaContentTypeElements.isEmpty()) {
                            System.out.println("Meta Content-Type: " + metaContentTypeElements.first().attr("content"));
                        }
                        
                        // 提取所有链接
                        Elements links = document.select("a[href]");
                        System.out.println("\n找到的链接数量: " + links.size());
                        
                        // 打印前5个链接
                        System.out.println("前5个链接:");
                        int linkCount = 0;
                        for (Element link : links) {
                            if (linkCount < 5) {
                                System.out.println(link.text() + " -> " + link.absUrl("href"));
                                linkCount++;
                            } else {
                                break;
                            }
                        }
                        
                        // 提取所有表格
                        Elements tables = document.select("table");
                        System.out.println("\n找到的表格数量: " + tables.size());
                        
                        // 如果有表格，打印第一个表格的基本结构
                        if (!tables.isEmpty()) {
                            Element firstTable = tables.first();
                            Elements rows = firstTable.select("tr");
                            System.out.println("第一个表格的行数: " + rows.size());
                            
                            // 检查是否有表头
                            Elements tableHeaders = firstTable.select("th");
                            if (!headers.isEmpty()) {
                                List<String> headerTexts = new ArrayList<>();
                                for (Element header : tableHeaders) {
                                    headerTexts.add(header.text().trim());
                                }
                                System.out.println("表头内容: " + String.join(", ", headerTexts));
                            }
                        }
                        
                        // 使用HtmlParserUtil工具类解析HTML（示例）
                        System.out.println("\n===== 使用HtmlParserUtil工具类解析HTML ======");
                        
                        // 提取标题
                        String extractedTitle = HtmlParserUtil.extractTitle(gb2312Content);
                        System.out.println("提取的标题: " + extractedTitle);
                        
                        // 提取链接
                        List<Map<String, String>> linksList = HtmlParserUtil.extractLinks(gb2312Content);
                        System.out.println("提取的链接数量: " + linksList.size());
                        System.out.println("前3个链接:");
                        for (int i = 0; i < Math.min(3, linksList.size()); i++) {
                            Map<String, String> linkMap = linksList.get(i);
                            System.out.println("  " + (i + 1) + ". " + linkMap.get("text") + " -> " + linkMap.get("href"));
                        }
                        
                        // 提取表格数据
                        List<List<List<String>>> tablesData = HtmlParserUtil.extractTables(gb2312Content);
                        System.out.println("提取的表格数量: " + tablesData.size());
                        
                        if (!tablesData.isEmpty()) {
                            List<List<String>> firstTable = tablesData.get(0);
                            System.out.println("第一个表格的行数: " + firstTable.size());
                            
                            // 打印表头和前3行数据
                            if (!firstTable.isEmpty()) {
                                System.out.println("表头: " + String.join(", ", firstTable.get(0)));
                                System.out.println("数据行示例:");
                                for (int i = 1; i < Math.min(4, firstTable.size()); i++) {
                                    System.out.println("  " + String.join(", ", firstTable.get(i)));
                                }
                            }
                        }
                        
                        // 检测编码
                        String detectedEncoding = HtmlParserUtil.detectEncoding(gb2312Content).name();
                        System.out.println("检测到的编码: " + detectedEncoding);
                        
                        // 使用工具类的综合解析方法
                        Map<String, Object> parseResult = HtmlParserUtil.parseHtmlFromBytes(bodyBytes, "GB2312");
                        System.out.println("\n综合解析结果:");
                        System.out.println("  标题: " + parseResult.get("title"));
                        System.out.println("  是否包含DOCTYPE: " + parseResult.get("hasDoctype"));
                        System.out.println("  使用编码: " + parseResult.get("encoding"));
                    }
                }
            } else {
                System.out.println("请求失败，状态码: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            System.out.println("请求异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}