package com.wash.entity;

import com.wash.entity.data.*;
import lombok.Data;

import java.util.List;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/8
 * @Description
 */
@Data
public class Series {
    private PayTb payTb;

    private CommodityOrdersTb commodityOrderTb;

    private List<OrdersTb> ordersTbs;

    private List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs;

    private List<VendorProfitSharingTb> vendorProfitSharingTbs;


    public Series(PayTb payTb) {
        this.payTb = payTb;
    }
}