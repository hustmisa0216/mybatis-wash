package com.wash.entity.statistics;

import lombok.Data;

import java.io.Serializable;
@Data
public class DailyPaperTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer date;
    private Integer cityId;
    private Integer siteId;
    private Integer newMachineCount;
    private Integer machineCount;
    private Integer userCount;
    private Integer washUserCount; //洗车人数  当天的
    private Integer washCount;    //洗车次数  当天的
    private Integer washDuration; //洗车时长   当天的
    private Integer washCostAmount;
    private Integer rechargeUserCount;//对应付费人数  当天的
    private Integer rechargeCount;
    private Integer rechargeAmount; //当天充
    private Integer vendorRechargeAmount; //当天充
    private Integer cashbackAmount;
    private Integer directAmount;
    private Integer cardAmount;
    private Long rechargeAmountTotal;          //累计冲
    private Long vendorRechargeAmountTotal;    //累计冲
    private Integer remainAmount;
    private Integer remainCallback;
    private Integer remainDirect;
    private Integer remainCard;
    private Integer purchaseVipCount;
    private Integer newVipUserCount;
    private Integer vipAmount;
    private Integer washManyCountUser;
    private Integer manyMachineTimeUser;
    private Integer userShareCount;
    private Integer washRate;
    private Integer purchaseRate;
    private Integer monthCardSale;
    private Integer quarterCardSale;
    private Integer halfYearCardSale;
    private Integer yearCardSale;
    private Integer regInfiltrate;
    private Integer payInfiltrate;
    private Integer washInfiltrate;
    private Integer payCoverageRate;
    private Integer washCoverageRate;
    private Integer allCarNum;
    private Integer allUserCount;
    private Integer allPayUsrCount;
    private Integer allWashUsrCount;
    private Integer regAndWashCount;
    private Integer regAndPayCount;
    private Integer perUseCount;
    private Integer perUseAmount;
    private Integer machineUserCount;
    private Integer machineUserPayCount;
    private Integer machineUserPayRate;
    private Integer machineUserPayAmount;
    private Integer oldUserPayAmount;
    private Integer oldUserWashed;
    private Integer machineUserPrice;
    private Integer oldUserPrice;
    private Integer refundAmount;
    private Integer refundCount;
    private Integer vendorRefundAmount;
    private Integer firstWashUserCount;
    private Integer allVipUserCount;
    private Integer regAndPayAmount;
    private Long machineBreakDownDuration;
    private Long machineRunDuration;
    private Integer curMonthPayAmount;      //当月的支付金额
    private Integer vendorCurMonthPayAmount;//当月的 当月每天都要搞。。
    private Integer curMonthWashUserCount;
    private Integer curMonthRechargeUserCount;
    private Integer mallSuitAmount;
    private Integer mallEntityAmount;
    private Integer machineWithoutFreeUser;
    private Integer machineWithoutFreeUserPayCount;
    private Integer machineWithoutFreeUserPayAmount;
    private Integer machineWithoutFreeUserWashCount;
    private Integer machineWithoutFreeUserPrice;
    private Integer machineWithoutFreeUserWashRate;
    private Integer machineWithoutFreeUserPayRate;
    private Integer faMonthPayAmount;
    private Integer faPayAmount;
    private Long faPayAmountTotal;
    private Integer timingAmount;
    private Integer gasolineRechargeAmount;
    private Integer gasolinePayoutAmount;
    private Integer gasolineRefuelAmount;
    private Integer gasolineRemainAmount;
    private Integer gasolineRefuelUserCount;
}