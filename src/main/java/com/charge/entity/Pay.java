package com.charge.entity;

import lombok.Data;

@Data
public class Pay {
    private int id;
    private String tradeNo;
    private int uid;
    private Integer siteId;
    private int amount;
    private int refund;
    private byte type;
    private byte tradeType;
    private byte for_;
    private byte status;
    private String mchId;
    private String transactionId;
    private String payer;
    private Long callbackAt;
    private Long refundAt;
    private long createdAt;
    private long updatedAt;

    // Getters and Setters
}
