package com.charge.entity;

import lombok.Data;

@Data
public class StatementMonthly {
    private int id;
    private int date;
    private int cityId;
    private int siteId;
    private int plugCount;
    private int portCount;
    private int rechargeAmount;
    private int rechargeTimes;
    private int rechargeUserCount;
    private int rechargeNewUserCount;
    private int refundAmount;
    private int refundUserCount;
    private int chargeTimes;
    private int chargeDuration;
    private int chargeUserCount;
    private int chargeNewUserCount;
    private int chargeConsumeAmount;
    private int newUserCount;
    private int newUserRechargeCount;
    private int newUserChargeCount;
    private int userCountTotal;
    private int chargeTimesTotal;
    private int chargeUserCountTotal;
    private int rechargeAmountTotal;
    private int rechargeUserCountTotal;
    private int arpu;

    // Getters and Setters
}
