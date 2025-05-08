package com.wash.entity.constants;

public enum DeliveryMethodType {
    VIP_TIME(4, "VIP时间"),
    COUPON_WASHING(12, "洗车券"),
    PREPAID(10, "充值"), //冲会被拆分，当上一个单子钱不够，最后一笔洗会拆两单，commo里拆两个，但是v里只有一个
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


    /** SUB_TYPE
     * 	VendorProfitSharingSubTypeHistory        = 0  // 历史已结算汇总 ~20211109
     * 	VendorProfitSharingSubTypePayment        = 1  // 支付
     *
     *
     * 	VendorProfitSharingSubTypeBalance        = 2  // 尾款
     * 	VendorProfitSharingSubTypeRefund         = 3  // 退款
     * 	VendorProfitSharingSubTypeConsume        = 4  // 消耗
     * 	VendorProfitSharingSubTypeCrossSite      = 5  // 跨场地洗车
     *
     *
     * 	VendorProfitSharingSubTypeBalanceConsume = 6  // 余额洗车消耗
     * 	VendorProfitSharingSubTypeSMS            = 7  // 短信
     * 	VendorProfitSharingSubTypeAccessory      = 8  // 配件
     * 	VendorProfitSharingSubTypeBargainRefund  = 9  // 砍价退款
     * 	VendorProfitSharingSubTypeInvoicingTax   = 10 // 开票税金
     * 	VendorProfitSharingSubTypeOther          = 11 // 其他
     */
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