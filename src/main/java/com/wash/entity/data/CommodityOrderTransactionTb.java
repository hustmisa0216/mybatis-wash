package com.wash.entity.data;

import lombok.Data;

import java.io.Serializable;
@Data
public class CommodityOrderTransactionTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String orderId;
    private Integer siteId;
    private Integer deliveryMethod;
    private Integer flag;
    private Integer rechargeAmount;
    private Integer cashbackAmount;
    private Integer directAmount;
    private Integer cardAmount;
    private Integer depositAmount;
    private Integer amount;
    private Integer advanceRatio;
    private Integer rechargeAdvanceAmount;
    private Integer cashbackAdvanceAmount;
    private Integer directAdvanceAmount;
    private Integer cardAdvanceAmount;
    private Integer depositAdvanceAmount;
    private Integer advanceAmount;
    private Integer rechargeConsumeAmount;
    private Integer cashbackConsumeAmount;
    private Integer directConsumeAmount;
    private Integer cardConsumeAmount;
    private Integer depositConsumeAmount;
    private Integer consumeAmount;
    private Integer rechargeExpiredAmount;
    private Integer cashbackExpiredAmount;
    private Integer directExpiredAmount;
    private Integer cardExpiredAmount;
    private Integer depositExpiredAmount;
    private Integer expiredAmount;
    private Integer expiredCount;
    private Integer rechargeRemainAmount;
    private Integer cashbackRemainAmount;
    private Integer directRemainAmount;
    private Integer cardRemainAmount;
    private Integer depositRemainAmount;
    private Long createdAt;
    private Long updatedAt;

}