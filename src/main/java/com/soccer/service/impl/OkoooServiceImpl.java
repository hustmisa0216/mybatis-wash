package com.soccer.service.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.soccer.OkoooHttpClient;
import com.soccer.service.OkoooService;
import com.soccer.service.MatchDetailService;
import com.soccer.util.HtmlParserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.soccer.model.SoccerMatch;

/**
 * Okooo网站数据服务实现类
 */
@Service
public class OkoooServiceImpl implements OkoooService {

    private final OkoooHttpClient okoooHttpClient;
    private final MatchDetailService matchDetailService;

    @Autowired
    public OkoooServiceImpl(OkoooHttpClient okoooHttpClient, MatchDetailService matchDetailService) {
        this.okoooHttpClient = okoooHttpClient;
        this.matchDetailService = matchDetailService;
    }

    @Override
    public String getProcessedJingCaiData(String date) {
        // 调用httpclient获取原始HTML数据
        String rawData = okoooHttpClient.getJingCaiData(date);
        
        if (rawData == null || rawData.isEmpty()) {
            throw new RuntimeException("获取竞彩数据失败，返回内容为空");
        }
        
        // 对数据进行转换处理
        return extractAndTransformTableData(rawData);
    }

    @Override
    public String extractAndTransformTableData(String htmlContent) {
        // 使用Jsoup解析HTML内容
        Document document = Jsoup.parse(htmlContent);
        
        // 首先获取class="touzhu"的标签
        Elements touzhuElements = document.select(".touzhu_1");
        
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("共找到 " + touzhuElements.size() + " 个touzhu_1元素\n\n");
        
        // 存储解析出的比赛信息
        List<SoccerMatch> matches = new ArrayList<>();
        
        // 遍历每个touzhu元素，解析比赛信息
        for (Element element : touzhuElements) {
            SoccerMatch match = new SoccerMatch();
            
            // 解析比赛ID
            // 优先使用data-mid属性作为matchId
            String matchId = element.attr("data-mid");
            if (!matchId.isEmpty()) {
                match.setMatchId(matchId);
            } else {
                // 使用.xulie元素作为备选
                Element xulieElement = element.selectFirst(".xulie");
                if (xulieElement != null) {
                    match.setMatchId(xulieElement.text());
                }
            }
            
            // 解析赛事名称
            Element leagueElement = element.selectFirst(".saiming.aochao");
            if (leagueElement != null) {
                match.setLeagueName(leagueElement.text());
            }
            
            // 解析比赛时间
            Element timeElement = element.selectFirst(".shijian");
            if (timeElement != null) {
                match.setMatchTime(timeElement.text());
                // 从title属性中提取日期
                String title = timeElement.attr("title");
                if (title.contains("比赛时间:")) {
                    String dateTime = title.substring(5).trim();
                    if (dateTime.length() >= 10) {
                        match.setMatchDate(dateTime.substring(0, 10));
                    }
                }
            }
            
            // 解析主队信息
            Element homeTeamElement = element.selectFirst(".zhu.weiks .zhum.fff.hui_colo");
            if (homeTeamElement != null) {
                // 尝试从title属性获取完整名称
                String fullHomeTeamName = homeTeamElement.attr("title");
                if (!fullHomeTeamName.isEmpty()) {
                    match.setHomeTeam(fullHomeTeamName.trim());
                } else {
                    // 如果title为空，则使用元素文本
                    match.setHomeTeam(homeTeamElement.text().trim());
                }
            } else {
                // 尝试从data-hname属性获取
                String homeTeam = element.attr("data-hname");
                if (!homeTeam.isEmpty()) {
                    match.setHomeTeam(homeTeam);
                }
            }
            
            // 解析主队排名
            Element homeRankElement = element.selectFirst(".zhu.weiks .paim.paim_sel p");
            if (homeRankElement != null) {
                match.setHomeTeamRank(homeRankElement.text());
            }
            
            // 解析客队信息
            Element awayTeamElement = element.selectFirst(".fu.weiks .zhum.fff.hui_colo");
            if (awayTeamElement != null) {
                // 尝试从title属性获取完整名称
                String fullAwayTeamName = awayTeamElement.attr("title");
                if (!fullAwayTeamName.isEmpty()) {
                    match.setAwayTeam(fullAwayTeamName.trim());
                } else {
                    // 如果title为空，则使用元素文本
                    match.setAwayTeam(awayTeamElement.text().trim());
                }
            } else {
                // 尝试从data-aname属性获取
                String awayTeam = element.attr("data-aname");
                if (!awayTeam.isEmpty()) {
                    match.setAwayTeam(awayTeam);
                }
            }
            
            // 解析客队排名
            Element awayRankElement = element.selectFirst(".fu.weiks .paim.paim_sel p");
            if (awayRankElement != null) {
                match.setAwayTeamRank(awayRankElement.text());
            }
            
            // 解析赔率信息
            // 主胜赔率 - 优化选择器，使用更精确的选择器以匹配HTML结构
            Element homeWinOddsElement = element.selectFirst(".zhu.weiks .peilv.fff.hui_colo.red_colo");
            if (homeWinOddsElement == null) {
                // 尝试备选选择器
                homeWinOddsElement = element.selectFirst(".zhu.weiks .peilv");
                if (homeWinOddsElement == null) {
                    // 再次尝试更简单的选择器
                    homeWinOddsElement = element.selectFirst(".zhu.weiks .red_colo");
                }
            }
            if (homeWinOddsElement != null) {
                match.setHomeWinOdds(homeWinOddsElement.text().trim());
            }
            
            // 平局赔率 - 优化选择器，使用更精确的选择器以匹配HTML结构
            Element drawOddsElement = element.selectFirst(".ping.weiks .peilv.fff.hui_colo.red_colo");
            if (drawOddsElement == null) {
                // 尝试备选选择器
                drawOddsElement = element.selectFirst(".ping.weiks .peilv");
                if (drawOddsElement == null) {
                    // 再次尝试更简单的选择器
                    drawOddsElement = element.selectFirst(".ping.weiks .red_colo");
                }
            }
            if (drawOddsElement != null) {
                match.setDrawOdds(drawOddsElement.text().trim());
            }
            
            // 客胜赔率 - 优化选择器，使用更精确的选择器以匹配HTML结构
            Element awayWinOddsElement = element.selectFirst(".fu.weiks .peilv.fff.hui_colo.red_colo");
            if (awayWinOddsElement == null) {
                // 尝试备选选择器
                awayWinOddsElement = element.selectFirst(".fu.weiks .peilv");
                if (awayWinOddsElement == null) {
                    // 再次尝试更简单的选择器
                    awayWinOddsElement = element.selectFirst(".fu.weiks .red_colo");
                }
            }
            if (awayWinOddsElement != null) {
                match.setAwayWinOdds(awayWinOddsElement.text().trim());
            }
            
            // 解析让球信息
            Elements handicapElements = element.select(".rangqiu");
            if (!handicapElements.isEmpty()) {
                match.setHandicap(handicapElements.get(0).text().trim());
            }
            
            // 解析让球后的赔率 - 优化选择器，使用更精确的选择器以匹配HTML结构
            Elements handicapOddsElements = element.select(".rangqiuspf .peilv.fff.hui_colo.red_colo");
            if (handicapOddsElements.isEmpty()) {
                // 尝试备选选择器
                handicapOddsElements = element.select(".rangqiuspf .peilv");
                if (handicapOddsElements.isEmpty()) {
                    // 再次尝试更简单的选择器
                    handicapOddsElements = element.select(".rangqiuspf .red_colo");
                }
            }
            if (handicapOddsElements.size() >= 3) {
                match.setHandicapHomeWinOdds(handicapOddsElements.get(0).text().trim());
                match.setHandicapDrawOdds(handicapOddsElements.get(1).text().trim());
                match.setHandicapAwayWinOdds(handicapOddsElements.get(2).text().trim());
            }
            
            // 获取比赛序号
            // 优先使用data-morder属性作为matchOrder
            String matchOrder = element.attr("data-morder");
     
            
            // 尝试从id属性中提取
            String id = element.attr("id");
            if (!id.isEmpty()) {
                // 假设id格式为"match_6004"这样的格式
                int lastUnderscoreIndex = id.lastIndexOf('_');
                if (lastUnderscoreIndex > 0 && lastUnderscoreIndex < id.length() - 1) {
                    match.setMatchId(id);
                }
            }
            
            // 如果有日期信息，获取详细数据并合并
            if (match.getMatchDate() != null && matchOrder != null) {
                match = matchDetailService.enrichMatchWithDetail(match, match.getMatchDate(), matchOrder);
            }
            
            matches.add(match);
        }
        
        // 格式化输出比赛信息
        for (int i = 0; i < matches.size(); i++) {
            SoccerMatch match = matches.get(i);
            resultBuilder.append("比赛 " + (i + 1) + ":\n");
            resultBuilder.append("  ID: " + (match.getMatchId() != null ? match.getMatchId() : "未知") + "\n");
            resultBuilder.append("  赛事: " + (match.getLeagueName() != null ? match.getLeagueName() : "未知") + "\n");
            resultBuilder.append("  日期: " + (match.getMatchDate() != null ? match.getMatchDate() : "未知") + "\n");
            resultBuilder.append("  时间: " + (match.getMatchTime() != null ? match.getMatchTime() : "未知") + "\n");
            resultBuilder.append("  主队: " + (match.getHomeTeam() != null ? match.getHomeTeam() : "未知") + " " + (match.getHomeTeamRank() != null ? match.getHomeTeamRank() : "") + "\n");
            resultBuilder.append("  客队: " + (match.getAwayTeam() != null ? match.getAwayTeam() : "未知") + " " + (match.getAwayTeamRank() != null ? match.getAwayTeamRank() : "") + "\n");
            resultBuilder.append("  让球: " + (match.getHandicap() != null ? match.getHandicap() : "未知") + "\n");
            resultBuilder.append("  赔率[胜/平/负]: " + (match.getHomeWinOdds() != null ? match.getHomeWinOdds() : "未知") + " / " + (match.getDrawOdds() != null ? match.getDrawOdds() : "未知") + " / " + (match.getAwayWinOdds() != null ? match.getAwayWinOdds() : "未知") + "\n");
            resultBuilder.append("  让球赔率[胜/平/负]: " + (match.getHandicapHomeWinOdds() != null ? match.getHandicapHomeWinOdds() : "未知") + " / " + (match.getHandicapDrawOdds() != null ? match.getHandicapDrawOdds() : "未知") + " / " + (match.getHandicapAwayWinOdds() != null ? match.getHandicapAwayWinOdds() : "未知") + "\n");
            
            // 添加详细数据信息
            if (match.getMatchDetail() != null) {
                resultBuilder.append("  详细数据:\n");
              //  resultBuilder.append("    比分: " + (match.getMatchDetail().getHomeScore() != null ? match.getMatchDetail().getHomeScore() : "0") + ":" + (match.getMatchDetail().getAwayScore() != null ? match.getMatchDetail().getAwayScore() : "0") + "\n");
                
                // 检查是否有有效的比分赔率数据
                boolean hasScoreOdds = match.getMatchDetail().getScoreOdd1_0() != null || 
                                      match.getMatchDetail().getScoreOdd2_0() != null || 
                                      match.getMatchDetail().getScoreOdd2_1() != null;
                
                // 检查是否有有效的半全场赔率数据
                boolean hasHalfFullOdds = match.getMatchDetail().getHalfFullOddHH() != null || 
                                         match.getMatchDetail().getHalfFullOddHD() != null || 
                                         match.getMatchDetail().getHalfFullOddHA() != null;
                
                // 检查是否有有效的总进球数赔率数据
                boolean hasTotalGoalsOdds = match.getMatchDetail().getTotalGoalsOdd0() != null || 
                                          match.getMatchDetail().getTotalGoalsOdd1() != null || 
                                          match.getMatchDetail().getTotalGoalsOdd2() != null;
                
                resultBuilder.append("    半全场赔率: " + (hasHalfFullOdds ? "已获取" : "未获取") + "\n");
                resultBuilder.append("    比分赔率: " + (hasScoreOdds ? "已获取" : "未获取") + "\n");
                resultBuilder.append("    总进球数赔率: " + (hasTotalGoalsOdds ? "已获取" : "未获取") + "\n");
            }
            
            resultBuilder.append("\n");
        }
        
        return resultBuilder.toString();
    }
}