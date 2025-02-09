package com.wash.entity.franchisee;

import lombok.Data;

import java.io.Serializable;

@Data
public class FranchiseeTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private Byte status;
    private Byte type;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
    private Byte settlementMode;
    private Byte settlementInterval;
    private Integer withdrawMax;
    private Byte displayIncome;
    private Integer flag;
    private Byte advanceSettlement;
    private Byte advanceSettlementMode;
    private Byte advanceSettlementRatio;
    private String openid;
    private String nickname;
    private String userName;
    private String avatar;
    private String payee;
    private String cardNo;
    private String bankName;
    private Integer settledAmount;
    private Integer withdrawAmount;
    private Integer waitWithdraw;
    private Integer advanceAmount;
    private Integer consumeAmount;
    private Integer deductionAmount;
    private Byte deductionRatio;
    private Integer commodityPricingDefaultSite;
    private Integer washingPricingDefaultSite;
    private Integer smsQuota;
    private Integer siteCount;
    private Integer machineCount;
    private Integer stmtRechargeAmount;
    private Integer stmtProfitAmount;
    private Integer stmtUnsettledAmount;
    private Integer rechargeAmount;
    private Integer estimatedAmount;
    private Integer settledAverageDay7;
}