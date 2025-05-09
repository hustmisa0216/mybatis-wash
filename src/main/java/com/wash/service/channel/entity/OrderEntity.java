package com.wash.service.channel.entity;

import com.charge.entity.Pay;
import com.wash.entity.data.CommodityOrderProfitSharingTb;
import com.wash.entity.data.CommodityOrdersTb;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.PayTb;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/5/9
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {

    private CommodityOrdersTb commodityOrdersTb;
    private List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs;
    private List<OrdersTb> ordersTbs;




}
