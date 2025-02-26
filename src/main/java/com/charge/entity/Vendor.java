package com.charge.entity;

import lombok.Data;

@Data
public class Vendor {
    private int id;
    private int uid;
    private String name;
    private String phone;
    private String contact;
    private String email;
    private int disable;
    private int profitSharingAmount;
    private int profitSharingIncomeAmount;
    private int profitSharingExpenseAmount;
    private int undrawnAmount;
    private int drawnAmount;
    private int depositAmount;
    private int drawMethod;
    private int drawSingleLimit;
    private int dataAuthority;
    private String payee;
    private String bankNo;
    private String bankName;
    private int chargePricingDefaultSite;
    private int commodityPricingDefaultSite;
    private long createdAt;
    private long updatedAt;

    // Getters and Setters
}
