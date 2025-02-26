package com.charge.entity;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class StatementSite {
    private int id;
    private int siteId;
    private int cityId;
    private int plugCount;
    private int portCount;
    private int userCount;
    private int userBalance;
    private int userCardCount;
    private int rechargeAmount;
    private int rechargeUserCount;
    private int refundAmount;
    private int refundUserCount;
    private int chargeTimes;
    private int chargeDuration;
    private int chargeUserCount;
    private int chargeConsumeAmount;
    private int commodityOrderConsumeExpired;
    private int chargeCrossSiteIncome;
    private int chargeCrossSiteExpense;
    private int profitSharingPaymentAmount;
    private int profitSharingBalanceAmount;
    private int plugServiceCharge;
    private int operationDays;
    private BigDecimal averagePersonChargeTimes;
    private BigDecimal averageDailyChargeTimes;
    private BigDecimal cvrRecharge;
    private BigDecimal cvrCharge;

    // Getters and Setters
}
