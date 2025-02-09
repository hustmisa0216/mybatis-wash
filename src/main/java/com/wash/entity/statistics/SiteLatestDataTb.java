package com.wash.entity.statistics;

import lombok.Data;

import java.io.Serializable;
@Data
public class SiteLatestDataTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer numIndex;
    private Integer date;
    private Integer siteId;
    private Integer cityId;
    private Integer machineCount;
    private Integer rechargeAmount;
    private Integer vendorRechargeAmount;
    private Integer vendorRechargeUserCount;
    private Integer totalPayUserCount;
    private Integer newUserCount;
    private Integer totalUserCount;
    private Long machineBreakDownDuration;
    private Long machineRunDuration;
    private Integer newUserRechargeAmount;
    private Integer refundAmount;
    private Integer vendorRefundAmount;
    private Integer refundUserCount;
    private Integer vendorRefundUserCount;
    private Integer washUserCount;
    private Integer washCount;
    private Integer washDuration;
    private Integer firstWashUserCount;
    private Integer newUserRechargeCount;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
}