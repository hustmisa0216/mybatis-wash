package com.wash.entity.data;

import lombok.Data;

import java.io.Serializable;
@Data
public class CommodityOrderConsumeTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String orderId;
    private Integer deliveryMethod;
    private Byte type;
    private Integer uid;
    private Integer siteId;
    private Byte crossSite;
    private Integer rechargeAmount;
    private Integer cashbackAmount;
    private Integer directAmount;
    private Integer cardAmount;
    private Integer depositAmount;
    private Integer usedAmount;
    private Byte runOut;
    private String usageOrderId;
    private Byte status;
    private Long createdAt;
}