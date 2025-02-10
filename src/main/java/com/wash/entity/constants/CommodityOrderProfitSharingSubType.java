package com.wash.entity.constants;

public enum CommodityOrderProfitSharingSubType {
    PAYMENT(1, "支付"),
    BALANCE(2, "尾款"),
    CONSUME(4, "消耗"),
    REFUND(3, "退款"),
    CROSS_SITE(5, "跨场地洗车"),
    BALANCE_CONSUME(6, "余额洗车消耗"),
    BARGAIN_REFUND(7, "砍价退款");

    private final int value;
    private final String description;

    CommodityOrderProfitSharingSubType(int value, String description) {
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