package com.soccer.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OkoooService测试类
 */
@SpringBootTest
class OkoooServiceTest {

    @Autowired
    private OkoooService okoooService;

    /**
     * 测试获取并处理竞彩数据的功能
     */
    @Test
    void testGetProcessedJingCaiData() {
        // 测试使用默认日期
        String defaultDate = "2016-01-02";
        String result = okoooService.getProcessedJingCaiData(defaultDate);
        
        // 验证结果不为空
        assertNotNull(result, "处理后的竞彩数据不应为空");
        assertFalse(result.isEmpty(), "处理后的竞彩数据不应为空字符串");
        
        // 验证结果包含表格信息
        assertTrue(result.contains("共提取到"), "处理结果应包含表格数量信息");
        assertTrue(result.contains("表格"), "处理结果应包含表格数据");
        
        System.out.println("使用日期 " + defaultDate + " 的测试结果: ");
        System.out.println(result);
        
        // 可以根据需要添加更多的断言来验证数据的正确性
    }

    /**
     * 测试表格数据提取和转换功能
     */
    @Test
    void testExtractAndTransformTableData() {
        // 创建一个简单的HTML测试数据
        String testHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>测试表格</title></head>\n" +
                "<body>\n" +
                "<table>\n" +
                "<tr><th>表头1</th><th>表头2</th></tr>\n" +
                "<tr><td>数据1</td><td>数据2</td></tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";
        
        // 调用方法进行测试
        String result = okoooService.extractAndTransformTableData(testHtml);
        
        // 验证结果
        assertNotNull(result, "表格数据提取结果不应为空");
        assertTrue(result.contains("共提取到 1 个表格"), "应提取到1个表格");
        assertTrue(result.contains("表头1"), "应包含表头1信息");
        assertTrue(result.contains("数据1"), "应包含数据1信息");
        
        System.out.println("表格数据提取测试结果: ");
        System.out.println(result);
    }
}