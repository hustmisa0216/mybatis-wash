package com.soccer.service;

/**
 * Okooo网站数据服务接口
 */
public interface OkoooService {
    
    /**
     * 获取并处理指定日期的竞彩数据
     * @param date 日期，格式为yyyy-MM-dd
     * @return 处理后的竞彩数据
     */
    String getProcessedJingCaiData(String date);
    
    /**
     * 从HTML内容中提取和转换表格数据
     * @param htmlContent HTML内容
     * @return 转换后的表格数据
     */
    String extractAndTransformTableData(String htmlContent);
}