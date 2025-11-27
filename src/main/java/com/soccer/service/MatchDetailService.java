package com.soccer.service;

import com.soccer.model.MatchDetail;
import com.soccer.model.SoccerMatch;

/**
 * 比赛详细信息服务接口，用于获取和处理比赛详细数据
 */
public interface MatchDetailService {
    
    /**
     * 根据比赛信息获取详细数据并合并到SoccerMatch对象中
     * @param match 比赛对象
     * @param date 比赛日期
     * @param matchOrder 比赛序号
     * @return 合并了详细数据的比赛对象
     */
    SoccerMatch enrichMatchWithDetail(SoccerMatch match, String date, String matchOrder);
    
    /**
     * 解析详细页面HTML，提取比赛详细信息
     * @param htmlContent 详细页面HTML内容
     * @return 比赛详细信息对象
     */
    MatchDetail parseMatchDetail(String htmlContent);
    
    /**
     * 从详细页面获取比分信息
     * @param htmlContent 详细页面HTML内容
     * @return 比分信息（主队:客队）
     */
    String getMatchScore(String htmlContent);
}