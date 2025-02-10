package com.wash.entity.data;

import lombok.Data;

import java.io.Serializable;
@Data
public class CommodityOrdersTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String orderId;
    private String depositOrderId;
    private Integer uid;
    private Integer cid;
    private Integer siteId;
    private String paySn;
    private Byte status;
    private String expressNum;
    private Long expressdAt;
    private Integer revUid;
    private String revName;
    private String phone;
    private String addr;
    private Integer paymentType;
    private String ext;
    private String voucherId;
    private Integer paymentDeposit;
    private Integer paymentMoney;
    private Integer paymentBalance;
    private Integer paymentTotal;
    private Integer priceTotal;
    private Integer refundAmount;
    private Long createdAt;
    private Long deletedAt;
    private Long updatedAt;
    private Integer vipDuration;
    private Long vipExpiredAt;
    private Integer vipQuota;
    private Byte deliveryStatus;
    private Integer perUseCard;
    private Integer perUseCardDuration;
    private Long perUseCardExpiredAt;
    private Integer washerFluid;
    private Integer timing;
    private Integer timingDuration;
    private Long timingExpiredAt;
    private Integer prepaidDuration;
    private Long prepaidExpiredAt;
    private Integer couponRule;
    private Integer couponDuration;
    private Integer usedAmount;
    private Integer usedQuota;
    private Integer skuId;
    private Integer count;
    private Integer parentId;
    private Integer suitId;
    private String remark;
    private Integer expressFee;
    private Integer freightCost;
    private String sets;
    private Integer flag;

}