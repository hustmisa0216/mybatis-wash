package com.soccer.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTML解析工具类，提供常用的HTML内容解析功能
 */
public class HtmlParserUtil {

    /**
     * 解析HTML字符串为Document对象
     * @param htmlContent HTML内容字符串
     * @return Jsoup Document对象
     */
    public static Document parseHtml(String htmlContent) {
        return Jsoup.parse(htmlContent);
    }

    /**
     * 从HTML内容中提取标题
     * @param htmlContent HTML内容字符串
     * @return 页面标题
     */
    public static String extractTitle(String htmlContent) {
        Document document = parseHtml(htmlContent);
        return document.title();
    }

    /**
     * 从HTML内容中提取所有链接
     * @param htmlContent HTML内容字符串
     * @return 链接列表，键为链接文本，值为链接URL
     */
    public static List<Map<String, String>> extractLinks(String htmlContent) {
        List<Map<String, String>> linksList = new ArrayList<>();
        Document document = parseHtml(htmlContent);
        Elements links = document.select("a[href]");
        
        for (Element link : links) {
            Map<String, String> linkMap = new HashMap<>();
            linkMap.put("text", link.text());
            linkMap.put("href", link.absUrl("href"));
            linksList.add(linkMap);
        }
        
        return linksList;
    }

    /**
     * 从HTML内容中提取所有表格数据
     * @param htmlContent HTML内容字符串
     * @return 表格数据列表，每个表格是一个二维列表
     */
    public static List<List<List<String>>> extractTables(String htmlContent) {
        List<List<List<String>>> tablesData = new ArrayList<>();
        Document document = parseHtml(htmlContent);
        Elements tables = document.select("table");
        
        for (Element table : tables) {
            List<List<String>> tableData = new ArrayList<>();
            Elements rows = table.select("tr");
            
            for (Element row : rows) {
                List<String> rowData = new ArrayList<>();
                Elements cells = row.select("th, td");
                
                for (Element cell : cells) {
                    rowData.add(cell.text().trim());
                }
                
                if (!rowData.isEmpty()) {
                    tableData.add(rowData);
                }
            }
            
            if (!tableData.isEmpty()) {
                tablesData.add(tableData);
            }
        }
        
        return tablesData;
    }

    /**
     * 从HTML内容中提取指定CSS选择器的元素
     * @param htmlContent HTML内容字符串
     * @param cssSelector CSS选择器
     * @return 匹配的元素列表
     */
    public static List<String> extractElementsBySelector(String htmlContent, String cssSelector) {
        List<String> elements = new ArrayList<>();
        Document document = parseHtml(htmlContent);
        Elements selectedElements = document.select(cssSelector);
        
        for (Element element : selectedElements) {
            elements.add(element.text().trim());
        }
        
        return elements;
    }

    /**
     * 检测HTML内容的编码
     * @param htmlContent HTML内容字符串
     * @return 检测到的编码
     */
    public static Charset detectEncoding(String htmlContent) {
        Document document = parseHtml(htmlContent);
        
        // 检查meta标签中的charset属性
        Elements metaCharsetElements = document.select("meta[charset]");
        if (!metaCharsetElements.isEmpty()) {
            try {
                String charset = metaCharsetElements.first().attr("charset").trim();
                return Charset.forName(charset);
            } catch (Exception e) {
                // 忽略编码解析错误
            }
        }
        
        // 检查meta标签中的content-type属性
        Elements metaContentTypeElements = document.select("meta[http-equiv=Content-Type]");
        if (!metaContentTypeElements.isEmpty()) {
            String contentType = metaContentTypeElements.first().attr("content");
            if (contentType != null) {
                String[] parts = contentType.split("charset=");
                if (parts.length > 1) {
                    try {
                        String charset = parts[1].trim().split(";").length > 0 ? 
                                         parts[1].trim().split(";")[0] : parts[1].trim();
                        return Charset.forName(charset);
                    } catch (Exception e) {
                        // 忽略编码解析错误
                    }
                }
            }
        }
        
        // 默认返回UTF-8
        return StandardCharsets.UTF_8;
    }

    /**
     * 清理HTML内容，去除标签，只保留文本
     * @param htmlContent HTML内容字符串
     * @return 清理后的纯文本
     */
    public static String cleanHtml(String htmlContent) {
        Document document = parseHtml(htmlContent);
        return document.text();
    }

    /**
     * 从HTML字节数组中解析内容并提取信息
     * @param htmlBytes HTML内容字节数组
     * @param charset 字符集
     * @return 包含解析结果的Map
     */
    public static Map<String, Object> parseHtmlFromBytes(byte[] htmlBytes, String charset) {
        Map<String, Object> resultMap = new HashMap<>();
        
        try {
            String htmlContent = new String(htmlBytes, charset);
            Document document = parseHtml(htmlContent);
            
            resultMap.put("title", document.title());
            resultMap.put("links", extractLinks(htmlContent));
            resultMap.put("tables", extractTables(htmlContent));
            resultMap.put("cleanText", cleanHtml(htmlContent));
            resultMap.put("hasDoctype", htmlContent.contains("<!DOCTYPE"));
            resultMap.put("encoding", charset);
            
        } catch (Exception e) {
            resultMap.put("error", e.getMessage());
        }
        
        return resultMap;
    }
}