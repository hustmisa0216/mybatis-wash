package com.soccer.service.impl;

import com.soccer.OkoooHttpClient;
import com.soccer.model.MatchDetail;
import com.soccer.model.SoccerMatch;
import com.soccer.service.MatchDetailService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;

/**
 * 比赛详细信息服务实现类，用于获取和处理比赛详细数据
 */
@Service
public class MatchDetailServiceImpl implements MatchDetailService {
    
    private final OkoooHttpClient okoooHttpClient;
    
    @Autowired
    public MatchDetailServiceImpl(OkoooHttpClient okoooHttpClient) {
        this.okoooHttpClient = okoooHttpClient;
    }
    
    @Override
    public SoccerMatch enrichMatchWithDetail(SoccerMatch match, String date, String matchOrder) {
        if (match == null) {
            System.err.println("enrichMatchWithDetail: 传入的match对象为null");
            return null;
        }
        
   
        if (date == null || date.isEmpty()) {
            System.err.println("enrichMatchWithDetail: 日期参数为空");
            return match;
        }
        
        if (matchOrder == null || matchOrder.isEmpty()) {
            System.err.println("enrichMatchWithDetail: 比赛场次参数为空");
            return match;
        }
        
        try {
            // 构造详细数据URL
            String url = constructDetailUrl(date, matchOrder);
            System.out.println("enrichMatchWithDetail: 请求比赛详细数据URL: " + url);
            
            // 获取详细页面HTML内容
            String htmlContent = okoooHttpClient.getMatchDetailData(url);
            
            if (htmlContent == null || htmlContent.isEmpty()) {
                System.err.println("enrichMatchWithDetail: 获取比赛详细HTML为空");
                return match;
            }
            
            // 解析详细数据
            MatchDetail matchDetail = parseMatchDetail(htmlContent);
            
            // 设置比赛比分
            String score = getMatchScore(htmlContent);
            if (score != null && !score.isEmpty()) {
                String[] scoreParts = score.split(":");
                if (scoreParts.length == 2) {
                    // matchDetail.setHomeScore(scoreParts[0].trim());
                    // matchDetail.setAwayScore(scoreParts[1].trim());
                }
            }
            
            // 验证解析结果
            boolean hasValidOdds = false;
            
            // 检查比分赔率是否存在有效数据
            if (matchDetail.getScoreOdd1_0() != null || matchDetail.getScoreOdd2_0() != null || 
                matchDetail.getScoreOdd2_1() != null || matchDetail.getScoreOdd3_0() != null) {
                hasValidOdds = true;
            }
            
            // 检查半全场赔率是否存在有效数据
            if (matchDetail.getHalfFullOddHH() != null || matchDetail.getHalfFullOddHD() != null || 
                matchDetail.getHalfFullOddHA() != null || matchDetail.getHalfFullOddDH() != null) {
                hasValidOdds = true;
            }
            
            // 检查总进球数赔率是否存在有效数据
            if (matchDetail.getTotalGoalsOdd0() != null || matchDetail.getTotalGoalsOdd1() != null || 
                matchDetail.getTotalGoalsOdd2() != null || matchDetail.getTotalGoalsOdd3() != null) {
                hasValidOdds = true;
            }
            
            if (!hasValidOdds) {
                System.err.println("enrichMatchWithDetail: 解析比赛详细数据后赔率信息为空");
            } else {
                System.out.println("enrichMatchWithDetail: 成功解析比赛详细数据，已设置比分赔率、半全场赔率和总进球数赔率");
            }
            
            // 将详细数据合并到比赛对象中
            match.setMatchDetail(matchDetail);
        } catch (Exception e) {
            System.err.println("获取比赛详细数据失败(ID: " + matchOrder + "): " + e.getMessage());
            e.printStackTrace();
        }
        
        return match;
    }
    
    @Override
    public MatchDetail parseMatchDetail(String htmlContent) {
        MatchDetail matchDetail = new MatchDetail();
        
        try {
            // 处理可能的编码问题
            if (htmlContent != null && !htmlContent.isEmpty()) {
                // 尝试将内容转换为UTF-8编码
                
                Document document = Jsoup.parse(htmlContent);
            
                // 解析比分赔率
                parseScoreOdds(document, matchDetail);
                
                // 解析半全场赔率
                parseHalfFullOdds(document, matchDetail);
                
                // 解析总进球数赔率
                parseTotalGoalsOdds(document, matchDetail);
            }
        } catch (Exception e) {
            System.err.println("解析比赛详细数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return matchDetail;
    }
    
    @Override
    public String getMatchScore(String htmlContent) {
        try {
            Document document = Jsoup.parse(htmlContent);
            
            // 尝试多个可能的选择器来获取比分信息
            Element scoreElement = null;
            
            // 尝试常见的比分相关选择器
            scoreElement = document.selectFirst(".match_score");
            if (scoreElement == null) {
                scoreElement = document.selectFirst(".score");
            }
            if (scoreElement == null) {
                scoreElement = document.selectFirst(".bs");
            }
            if (scoreElement == null) {
                // 尝试查找包含比分格式的元素（例如 "2:1"）
                Elements elements = document.select("*:containsOwn(:)");
                for (Element element : elements) {
                    String text = element.text().trim();
                    if (text.matches("^\\d+\\:\\d+$")) {
                        scoreElement = element;
                        break;
                    }
                }
            }
            
            if (scoreElement != null) {
                String score = scoreElement.text().trim();
                // 确保是有效的比分格式（例如 "2:1"）
                if (score.matches("^\\d+\\:\\d+$")) {
                    return score;
                }
            }
            
            System.out.println("getMatchScore: 未找到有效的比分元素");
        } catch (Exception e) {
            System.err.println("获取比赛比分失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 构造比赛详细数据URL
     */
    private String constructDetailUrl(String date, String matchOrder) {
        // 格式: https://www.okooo.cn/jingcai/?action=more&LotteryNo=2016-01-02&MatchOrder=6004
        return "https://www.okooo.cn/jingcai/?action=more&LotteryNo=" + date + "&MatchOrder=" + matchOrder;
    }
    

    
    /**
     * 解析比分赔率
     */
    private void parseScoreOdds(Document document, MatchDetail matchDetail) {
        try {
            // 查找比分赔率区域 - 根据HTML结构，从mrfg元素开始定位
            Elements mrfgElements = document.select(".mrfg");
            
            if (!mrfgElements.isEmpty()) {
                Element mrfgElement = mrfgElements.first();
                // 在mrfg元素内查找包含比分的float_l元素
                Elements floatLElements = mrfgElement.select(".float_l");
                
                if (!floatLElements.isEmpty()) {
                    Element floatLElement = floatLElements.get(1);
                    // 查找float_l元素内的zk_1元素作为比分赔率容器，这些zk_1元素分别包含胜、平、负的赔率
                    Elements zk1Elements = floatLElement.select(".zk_1");
                    
                    if (!zk1Elements.isEmpty()) {
                        // 遍历所有zk_1元素，这些元素分别包含胜、平、负的赔率
                        for (int i = 0; i < zk1Elements.size(); i++) {
                            Element scoreContainer = zk1Elements.get(i);
                            if (scoreContainer != null) {
                                // 根据zk_1元素的索引确定赔率类型：0-胜，1-平，2-负
                                String oddsType = "";
                                switch (i) {
                                    case 0: oddsType = "胜"; break;
                                    case 1: oddsType = "平"; break;
                                    case 2: oddsType = "负"; break;
                                }
                                
                                // 在比分容器中查找所有ping.weiks元素，这些元素包含单个比分赔率项
                                Elements oddsItems = scoreContainer.select(".ping.weiks");
                                
                                if (!oddsItems.isEmpty()) {
                                    for (Element item : oddsItems) {
                                        // 在每个赔率项中查找标签和赔率值
                                        Element labelElement = item.selectFirst(".peilv.fff.hui_colo.red_colo");
                                        Element oddElement = item.selectFirst(".peilv_1.fff.hui_colo.red_colo");
                                        
                                        // 如果找不到精确匹配的类，尝试更宽松的选择器
                                        if (labelElement == null) {
                                            labelElement = item.selectFirst(".peilv");
                                        }
                                        if (oddElement == null) {
                                            oddElement = item.selectFirst(".peilv_1");
                                        }
                                        
                                        if (labelElement != null && oddElement != null) {
                                            String label = labelElement.text().trim();
                                            String odd = oddElement.text().trim();
                                            
                                            // 只添加有效的比分赔率（通常包含数字）
                                            if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(odd) ) {
                                                // 处理"其他"赔率情况
                                                if (label.contains("其他")) {
                                                    // 根据赔率类型设置对应的其他赔率
                                                    if (oddsType.equals("胜")) {
                                                        matchDetail.setScoreOddOthersWin(odd);
                                                    } else if (oddsType.equals("平")) {
                                                        matchDetail.setScoreOddOthersDraw(odd);
                                                    } else if (oddsType.equals("负")) {
                                                        matchDetail.setScoreOddOthersLose(odd);
                                                    }
                                                } else if((label.matches(".*\\d.*") || odd.matches(".*\\d.*\\.\\d+.*"))){
                                                    // 普通比分赔率
                                                    matchDetail.setScoreOdd(label, odd);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 如果找不到ping.weiks元素，则尝试原始的解析方法作为后备
                        if (matchDetail.getScoreOdd0_0() == null && matchDetail.getScoreOdd1_0() == null) {
                            Elements scoreElements = document.select(".peilv");
                            Elements labelElements = document.select(".spf_main_cont > div:not(.peilv)");
                            
                            if (labelElements.size() > 0 && scoreElements.size() > 0) {
                                // 确保标签和赔率数量匹配
                                int minSize = Math.min(labelElements.size(), scoreElements.size());
                                for (int i = 0; i < minSize; i++) {
                                    String label = labelElements.get(i).text().trim();
                                    String odd = scoreElements.get(i).text().trim();
                                    
                                    // 只添加有效的比分赔率（通常包含数字和可能的中文）
                                    if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(odd) && 
                                        (label.matches(".*\\d.*") || odd.matches(".*\\d.*\\.\\d+.*"))) {
                                        matchDetail.setScoreOdd(label, odd);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析比分赔率时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 解析半全场赔率
     */
    private void parseHalfFullOdds(Document document, MatchDetail matchDetail) {
        try {
            // 查找半全场标签（使用类名zk_1和float_l，索引1表示第二个元素）
            Elements halfFullLabels = document.select(".zk_1.float_l");
            
            if (!halfFullLabels.isEmpty() && halfFullLabels.size() > 1) {
                // 获取半全场标签的父元素，确定半全场赔率区域
                Element halfFullContainer = halfFullLabels.get(1).parent();
                
                if (halfFullContainer != null) {
                    // 在半全场容器中查找所有ping.weiks元素，这些元素包含单个半全场赔率项
                    Elements oddsItems = halfFullContainer.select(".ping.weiks");
                    
                    if (!oddsItems.isEmpty()) {
                        for (Element item : oddsItems) {
                            // 在每个赔率项中查找标签和赔率值
                            Element labelElement = item.selectFirst(".peilv.fff.hui_colo.red_colo");
                            Element oddElement = item.selectFirst(".peilv_1.fff.hui_colo.red_colo");
                            
                            // 如果找不到精确匹配的类，尝试更宽松的选择器
                            if (labelElement == null) {
                                labelElement = item.selectFirst(".peilv");
                            }
                            if (oddElement == null) {
                                oddElement = item.selectFirst(".peilv_1");
                            }
                            
                            if (labelElement != null && oddElement != null) {
                                String label = labelElement.text().trim();
                                String odd = oddElement.text().trim();
                                
                                if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(odd)) {
                                    // 转换标签格式，将"胜/胜"转换为系统内部格式"胜胜"
                                    String normalizedLabel = label.replace("/", "");
                                    matchDetail.setHalfFullOdd(normalizedLabel, odd);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析半全场赔率时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 解析总进球数赔率
     */
    private void parseTotalGoalsOdds(Document document, MatchDetail matchDetail) {
        try {
            // 查找总进球数标签（使用类名zk_1和float_l，索引0表示第一个元素）
            Elements totalGoalsLabels = document.select(".zk_1.float_l");
            
            if (!totalGoalsLabels.isEmpty()) {
                // 获取总进球数标签的父元素，确定总进球数赔率区域
                Element totalGoalsContainer = totalGoalsLabels.get(0).parent();
                
                if (totalGoalsContainer != null) {
                    // 在总进球数容器中查找所有ping.weiks元素，这些元素包含单个总进球数赔率项
                    Elements oddsItems = totalGoalsContainer.select(".ping.weiks");
                    
                    if (!oddsItems.isEmpty()) {
                        for (Element item : oddsItems) {
                            // 在每个赔率项中查找标签和赔率值
                            Element labelElement = item.selectFirst(".peilv.fff.hui_colo.red_colo");
                            Element oddElement = item.selectFirst(".peilv_1.fff.hui_colo.red_colo");
                            
                            // 如果找不到精确匹配的类，尝试更宽松的选择器
                            if (labelElement == null) {
                                labelElement = item.selectFirst(".peilv");
                            }
                            if (oddElement == null) {
                                oddElement = item.selectFirst(".peilv_1");
                            }
                            
                            if (labelElement != null && oddElement != null) {
                                String label = labelElement.text().trim();
                                String v=label.replace("总", "").replace("球", "");
                                String odd = oddElement.text().trim();
                                
                                if (StringUtils.isNotBlank(v) && StringUtils.isNotBlank(odd)) {
                                    matchDetail.setTotalGoalsOdd(v, odd);
                                }
                            }
                        }
                    }
                    
                    // 如果找不到ping.weiks元素，则尝试原始的解析方法作为后备
                    if (matchDetail.getTotalGoalsOdd0() == null && matchDetail.getTotalGoalsOdd1() == null) {
                        Elements totalGoalsElements = totalGoalsContainer.select(".peilv_1");
                        Elements totalGoalsLabelsBackup = totalGoalsContainer.select(".peilv");
                        
                        if (totalGoalsLabelsBackup.size() > 0 && totalGoalsElements.size() > 0) {
                            int minSize = Math.min(totalGoalsLabelsBackup.size(), totalGoalsElements.size());
                            for (int i = 0; i < minSize; i++) {
                                String label = totalGoalsLabelsBackup.get(i).text().trim();
                                String odd = totalGoalsElements.get(i).text().trim();
                                
                                if (StringUtils.isNotBlank(label) && StringUtils.isNotBlank(odd)) {
                                    matchDetail.setTotalGoalsOdd(label, odd);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析总进球数赔率时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}