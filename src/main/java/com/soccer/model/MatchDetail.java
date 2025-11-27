package com.soccer.model;

import lombok.Data;

/**
 * 比赛详细信息实体类，用于存储从详细页面解析出的比分、赔率、半全场等信息
 */
@Data
public class MatchDetail {

    // 比分赔率信息 - 单独字段表示
    private String scoreOdd1_0;
    private String scoreOdd2_0;
    private String scoreOdd2_1;
    private String scoreOdd3_0;
    private String scoreOdd3_1;
    private String scoreOdd3_2;
    private String scoreOdd4_0;
    private String scoreOdd4_1;
    private String scoreOdd4_2;
    private String scoreOdd5_0;
    private String scoreOdd0_0;
    private String scoreOdd0_1;
    private String scoreOdd1_1;
    private String scoreOdd0_2;
    private String scoreOdd1_2;
    private String scoreOdd0_3;
    private String scoreOdd1_3;
    private String scoreOdd2_2;
    private String scoreOdd2_3;
    private String scoreOdd3_3;
    private String scoreOddOthers;
    private String scoreOddOthersWin; // 胜其他赔率
    private String scoreOddOthersDraw; // 平其他赔率
    private String scoreOddOthersLose; // 负其他赔率
    
    // 总进球数赔率 - 单独字段表示
    private String totalGoalsOdd0;
    private String totalGoalsOdd1;
    private String totalGoalsOdd2;
    private String totalGoalsOdd3;
    private String totalGoalsOdd4;
    private String totalGoalsOdd5;
    private String totalGoalsOdd6;
    private String totalGoalsOdd7Plus;
    
    // 半全场赔率 - 单独字段表示
    private String halfFullOddHH; // 胜胜
    private String halfFullOddHD; // 胜负
    private String halfFullOddHA; // 胜平
    private String halfFullOddDH; // 平胜
    private String halfFullOddDD; // 平平
    private String halfFullOddDA; // 平负
    private String halfFullOddAH; // 负胜
    private String halfFullOddAD; // 负平
    private String halfFullOddAA; // 负负

    // 设置比分赔率的方法
    public void setScoreOdd(String score, String odds) {
        if (score == null || odds == null) return;
        
        switch (score) {
            case "1-0": setScoreOdd1_0(odds); break;
            case "2-0": setScoreOdd2_0(odds); break;
            case "2-1": setScoreOdd2_1(odds); break;
            case "3-0": setScoreOdd3_0(odds); break;
            case "3-1": setScoreOdd3_1(odds); break;
            case "3-2": setScoreOdd3_2(odds); break;
            case "4-0": setScoreOdd4_0(odds); break;
            case "4-1": setScoreOdd4_1(odds); break;
            case "4-2": setScoreOdd4_2(odds); break;
            case "5-0": setScoreOdd5_0(odds); break;
            case "0-0": setScoreOdd0_0(odds); break;
            case "0-1": setScoreOdd0_1(odds); break;
            case "1-1": setScoreOdd1_1(odds); break;
            case "0-2": setScoreOdd0_2(odds); break;
            case "1-2": setScoreOdd1_2(odds); break;
            case "0-3": setScoreOdd0_3(odds); break;
            case "1-3": setScoreOdd1_3(odds); break;
            case "2-2": setScoreOdd2_2(odds); break;
            case "2-3": setScoreOdd2_3(odds); break;
            case "3-3": setScoreOdd3_3(odds); break;
            default: setScoreOddOthers(odds); break;
        }
    }
    
    // 设置总进球数赔率的方法
    public void setTotalGoalsOdd(String goals, String odds) {
        if (goals == null || odds == null) return;
        
        switch (goals) {
            case "0": setTotalGoalsOdd0(odds); break;
            case "1": setTotalGoalsOdd1(odds); break;
            case "2": setTotalGoalsOdd2(odds); break;
            case "3": setTotalGoalsOdd3(odds); break;
            case "4": setTotalGoalsOdd4(odds); break;
            case "5": setTotalGoalsOdd5(odds); break;
            case "6": setTotalGoalsOdd6(odds); break;
            case "7+": setTotalGoalsOdd7Plus(odds); break;
            default: setTotalGoalsOdd7Plus(odds); break;
        }
    }
    
    // 设置半全场赔率的方法
    public void setHalfFullOdd(String result, String odds) {
        if (result == null || odds == null) return;
        
        switch (result) {
            case "胜胜": setHalfFullOddHH(odds); break;
            case "胜负": setHalfFullOddHD(odds); break;
            case "胜平": setHalfFullOddHA(odds); break;
            case "平胜": setHalfFullOddDH(odds); break;
            case "平平": setHalfFullOddDD(odds); break;
            case "平负": setHalfFullOddDA(odds); break;
            case "负胜": setHalfFullOddAH(odds); break;
            case "负平": setHalfFullOddAD(odds); break;
            case "负负": setHalfFullOddAA(odds); break;
            default: break;
        }
    }
}