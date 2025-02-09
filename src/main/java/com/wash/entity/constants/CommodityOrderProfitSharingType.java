package com.wash.entity.constants;

public enum CommodityOrderProfitSharingType {
    INCOME(1, "收入"),
    EXPENSE(2, "支出");

    private final int value;
    private final String description;

    CommodityOrderProfitSharingType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}