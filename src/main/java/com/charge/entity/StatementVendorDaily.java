package com.charge.entity;

import lombok.Data;

@Data
public class StatementVendorDaily {
    private int id;
    private int date;
    private int vendorId;
    private int siteId;
    private int profitSharingIncomeAmount;
    private int profitSharingExpenseAmount;
    private int profitSharingTotalAmount;

    // Getters and Setters
}
