package com.charge.entity;

import lombok.Data;

@Data
public class VendorProfitSharing {
    private int id;
    private int vendorId;
    private int siteId;
    private int amount;
    private int type;
    private int subType;
    private String transactionId;
    private long createdAt;

    // Getters and Setters
}
