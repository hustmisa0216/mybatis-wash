package com.wash.entity.statistics;

import lombok.Data;

import java.io.Serializable;
@Data
public class MonthPaperTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer date;
    private Integer cityId;
    private Integer siteId;
    private Integer userCount;
    private Integer washUserCount;
    private Integer washCount;
    private Integer washDuration;
    private Integer rechargeUserCount;
    private Integer rechargeCount;
    private Integer rechargeAmount;
    private Integer vendorRechargeAmount;
    private Integer purchaseVipCount;
    private Integer newVipUserCount;
    private Integer vipAmount;
    private Integer monthCardSale;
    private Integer quarterCardSale;
    private Integer halfYearCardSale;
    private Integer yearCardSale;
    private Integer perUseCount;
    private Integer perUseAmount;
    private Integer payActivityUserCount;
    private Integer faEnsureIncome;
    private Integer cwEnsureIncome;
    private Integer ensureIncome;
    private Integer arpu;
    private Integer allCarNum;
    private Integer scanEqCodeUserCount;
    private Integer allUserCount;
    private Integer newAndPayUserCount;
    private Integer allThirdPayUsrCount;
    private Integer payCoverageRate;
    private Integer payActivityUserRate;
    private Integer userAvgPay;
    private Integer regRate;
    private Integer newAndPayUserRate;
    private Integer machineCount;
    private Integer refundAmount;
    private Integer refundCount;
    private Integer vendorRefundAmount;
    private Integer firstWashUserCount;
    private Integer totalWashUserCount;
    private Integer totalWashCount;
    private Long totalRechargeAmount;
    private Long vendorTotalRechargeAmount;
    private Integer historyVipUserCount;
    private Integer installedMachineCount;
    private Integer regAndPayUserCount;
    private Integer regAndWashCount;
    private Integer mallSuitAmount;
    private Integer mallEntityAmount;
    private Integer timingAmount;
    private Integer faPayAmount;
    private Long faPayAmountTotal;
    private Integer gasolineRechargeAmount;
    private Integer gasolineRefuelAmount;
}