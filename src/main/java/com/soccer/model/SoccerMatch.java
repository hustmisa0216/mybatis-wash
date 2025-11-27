package com.soccer.model;

import java.util.StringJoiner;
import com.soccer.model.MatchDetail;
import lombok.Data;

/**
 * 足球比赛实体类，用于存储从HTML中解析出的比赛信息
 */
@Data
public class SoccerMatch {
    // 比赛ID
    private String matchId;
    // 赛事名称
    private String leagueName;
    // 比赛时间
    private String matchTime;
    // 比赛日期
    private String matchDate;
    // 主队名称
    private String homeTeam;
    // 客队名称
    private String awayTeam;
    // 主队排名
    private String homeTeamRank;
    // 客队排名
    private String awayTeamRank;
    // 让球数
    private String handicap;
    // 主胜赔率
    private String homeWinOdds;
    // 平局赔率
    private String drawOdds;
    // 客胜赔率
    private String awayWinOdds;
    // 让球后主胜赔率
    private String handicapHomeWinOdds;
    // 让球后平局赔率
    private String handicapDrawOdds;
    // 让球后客胜赔率
    private String handicapAwayWinOdds;
    
    // 比赛详细信息
    private MatchDetail matchDetail;

    // getter和setter方法
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(String matchTime) {
        this.matchTime = matchTime;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getHomeTeamRank() {
        return homeTeamRank;
    }

    public void setHomeTeamRank(String homeTeamRank) {
        this.homeTeamRank = homeTeamRank;
    }

    public String getAwayTeamRank() {
        return awayTeamRank;
    }

    public void setAwayTeamRank(String awayTeamRank) {
        this.awayTeamRank = awayTeamRank;
    }

    public String getHandicap() {
        return handicap;
    }

    public void setHandicap(String handicap) {
        this.handicap = handicap;
    }

    public String getHomeWinOdds() {
        return homeWinOdds;
    }

    public void setHomeWinOdds(String homeWinOdds) {
        this.homeWinOdds = homeWinOdds;
    }

    public String getDrawOdds() {
        return drawOdds;
    }

    public void setDrawOdds(String drawOdds) {
        this.drawOdds = drawOdds;
    }

    public String getAwayWinOdds() {
        return awayWinOdds;
    }

    public void setAwayWinOdds(String awayWinOdds) {
        this.awayWinOdds = awayWinOdds;
    }

    public String getHandicapHomeWinOdds() {
        return handicapHomeWinOdds;
    }

    public void setHandicapHomeWinOdds(String handicapHomeWinOdds) {
        this.handicapHomeWinOdds = handicapHomeWinOdds;
    }

    public String getHandicapDrawOdds() {
        return handicapDrawOdds;
    }

    public void setHandicapDrawOdds(String handicapDrawOdds) {
        this.handicapDrawOdds = handicapDrawOdds;
    }


    public String getHandicapAwayWinOdds() {
        return handicapAwayWinOdds;
    }

    public void setHandicapAwayWinOdds(String handicapAwayWinOdds) {
        this.handicapAwayWinOdds = handicapAwayWinOdds;
    }

    public MatchDetail getMatchDetail() {
        return matchDetail;
    }

    public void setMatchDetail(MatchDetail matchDetail) {
        this.matchDetail = matchDetail;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SoccerMatch.class.getSimpleName() + "[", "]")
                .add("matchId='" + matchId + "'")
                .add("leagueName='" + leagueName + "'")
                .add("matchTime='" + matchTime + "'")
                .add("homeTeam='" + homeTeam + "'")
                .add("awayTeam='" + awayTeam + "'")
                .add("handicap='" + handicap + "'")
                .add("homeWinOdds='" + homeWinOdds + "'")
                .add("drawOdds='" + drawOdds + "'")
                .add("awayWinOdds='" + awayWinOdds + "'")
                .add("hasDetail=" + (matchDetail != null) + "")
                .toString();
    }
}