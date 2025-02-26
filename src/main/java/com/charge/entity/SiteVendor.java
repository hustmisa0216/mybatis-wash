package com.charge.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SiteVendor {
    private int id;
    private int siteId;
    private int vendorId;
    private int role;
    private BigDecimal ratio;
    private int profitSharingAmount;
    private int profitSharingPaymentAmount;
    private int profitSharingBalanceAmount;
    private int profitSharingRefundAmount;
    private int profitSharingCrossSiteIncome;
    private int profitSharingCrossSiteExpense;
    private int profitSharingServiceCharge;
    private int profitSharingIncomeAmount;
    private int profitSharingExpenseAmount;
    private long createdAt;
    private long updatedAt;
    private long deletedAt;

    // Getters and Setters
}
