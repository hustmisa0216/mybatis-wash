package com.charge.entity;

import lombok.Data;

import java.util.List;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/26
 * @Description
 */
@Data
public class CharEntity {

    private Pay pay;

    private CommodityOrder commodityOrder;
    private List<CommodityOrderProfitSharing> commodityOrderProfitSharingList;
    private List<VendorProfitSharing> vendorProfitSharingList;

    private List<ChargeOrder> chargeOrders;
}
