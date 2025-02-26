package com.charge.entity;

import lombok.Data;

@Data
public class CommodityOrder {
    private int id;
    private String orderId;
    private int uid;
    private Integer siteId;
    private int cid;
    private String tradeNo;
    private byte status;
    private int paymentType;
    private int paymentAmount;
    private int flag;
    private int profitSharingRatio;
    private int profitSharingAmount;
    private int usedAmount;
    private int refundAmount;
    private int duration;
    private byte usageStatus;
    private long effectedAt;
    private long expiredAt;
    private Long payAt;
    private String sets;
    private String extra;
    private String commodityName;
    private long createdAt;
    private long updatedAt;

    // Getters and Setters
}
