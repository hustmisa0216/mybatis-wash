package com.wash.entity.constants;

public enum DeliveryMethodType {
    VIP_TIME(4, "VIP时间"),
    COUPON_WASHING(12, "洗车券"),
    PREPAID(10, "充值"),
    PREPAID_SUIT(11, "洗车卡包月套装"),

    NULL(0, "无需发，比如谢谢"),
    REDPACKET(1, "红包"),
    VOUCHER(2, "优惠券"),
    EXPRESS(3, "快递"),
    DEPOSIT(5, "储值"),
    GROUND_LOCK(7, "地锁押金"),
    WASHER_FLUID(8, "玻璃水"),
    TIMING(9, "计时会员"),
    PER_USE_CARD(6, "次卡");

    private final int type;
    private final String description;

    DeliveryMethodType(int type, String description) {
        this.type = type;
        this.description = description;
    }


    public String getDescription() {
        return description;
    }

   public static  DeliveryMethodType  from(int type){
        for(DeliveryMethodType deliveryMethodType:DeliveryMethodType.values()){
            if(deliveryMethodType.type==type){
                return  deliveryMethodType;
            }
        }
        return null;
    }
}