package com.wash.entity;

import com.wash.entity.data.*;
import com.wash.entity.franchisee.FranchiseeSiteTb;
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
    private List<VendorProfitSharingTb> vendorProfitSharingTbs;

    private FranchiseeSiteTb franchiseeSiteTb;
    private List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs;

    public Series(PayTb payTb) {
        this.payTb = payTb;
    }
}