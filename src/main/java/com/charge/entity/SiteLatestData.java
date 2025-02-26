package com.charge.entity;

import lombok.Data;

@Data
public class SiteLatestData {
    private int id;
    private int date;
    private int numIndex;
    private int cityId;
    private int siteId;
    private int plugCount;
    private int portCount;
    private int portFree;
    private int rechargeAmount;
    private int rechargeUserCount;
    private int refundAmount;
    private int chargeTimes;
    private int chargeDuration;
    private int chargeUserCount;
    private int chargeConsumeAmount;
    private int newUserCount;

    // Getters and Setters
}
