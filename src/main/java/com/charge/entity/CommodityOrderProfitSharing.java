package com.charge.entity;

import lombok.Data;

@Data
public class CommodityOrderProfitSharing {
    private int id;
    private String transactionId;
    private int siteId;
    private String orderId;
    private int amount;
    private byte type;
    private byte subType;
    private String relatedId;
    private String vendor;
    private long createdAt;

    // Getters and Setters
}
