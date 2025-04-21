package com.wash.service.channel;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.data.CommodityOrderProfitSharingTb;
import com.wash.entity.data.CommodityOrdersTb;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.site.SiteTb;
import com.wash.entity.statistics.EnsureIncomeTb;
import com.wash.mapper.*;
import com.wash.mapper.channel.ChannelSiteTbMapper;
import com.wash.mapper.channel.ChannelTbMapper;
import com.wash.service.Selecter;
import com.wash.service.channel.entity.CAmount;
import com.wash.service.channel.entity.ChannelSiteTb;
import com.wash.service.channel.entity.ChannelTb;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/20
 * @Description
 */
@Component
@DS("wash")
public class Start {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private ChannelSiteTbMapper channelSiteTbMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private EnsureIncomeTbMapper ensureIncomeTbMapper;

    @Autowired
    private DateGenerator dateGenerator;
    @Autowired
    private ChannelTbMapper channelTbMapper;
    @Autowired
    private SiteTbMapper siteTbMapper;

    @Autowired
    private CommodityOrdersTbMapper commodityOrdersTbMapper;

    @Autowired
    private CommodityOrderProfitSharingTbMapper commodityOrderProfitSharingTbMapper;

    @Autowired
    private Selecter selecter;

    public String start(int channelId, int startDate, int endDate) throws ParseException {
        QueryWrapper<ChannelSiteTb> channelSiteTbQueryWrapper = new QueryWrapper<>();
        channelSiteTbQueryWrapper.eq("channel_id", channelId).isNull("deleted_at");
        List<ChannelSiteTb> channelSiteTbList = channelSiteTbMapper.selectList(channelSiteTbQueryWrapper);


        List<Integer> siteIds;
        if (CollectionUtils.isEmpty(channelSiteTbList)) {
            QueryWrapper<ChannelTb> channelTbQueryWrapper = new QueryWrapper<>();
            channelTbQueryWrapper.eq("id", channelId);
            ChannelTb channelTb = channelTbMapper.selectOne(channelTbQueryWrapper);
            List<String> siteids = Arrays.stream(channelTb.getSelfSite().split(",")).collect(Collectors.toList());
            QueryWrapper<SiteTb> siteTbQueryWrapper = new QueryWrapper<>();
            siteTbQueryWrapper.in("id", siteids);
            List<SiteTb> siteTbList = siteTbMapper.selectList(siteTbQueryWrapper);
            siteIds = siteTbList.stream().map(SiteTb::getId).collect(Collectors.toList());
        } else {
            siteIds = channelSiteTbList.stream().map(ChannelSiteTb::getSiteId).collect(Collectors.toList());
        }

        long dateTimeStart = SIMPLE_DATE_FORMAT.parse(startDate + "").getTime() / 1000;
        long dateTimeEnd = SIMPLE_DATE_FORMAT.parse(endDate + "").getTime() / 1000;

        for (int siteId : siteIds) {
            QueryWrapper<CommodityOrdersTb> commodityOrdersTbQueryWrapper = new QueryWrapper<>();
            commodityOrdersTbQueryWrapper.eq("site_id", siteId).ge("created_at", dateTimeStart).le("created_at", dateTimeEnd);
            List<CommodityOrdersTb> commodityOrdersTbs = commodityOrdersTbMapper.selectList(commodityOrdersTbQueryWrapper);
            commodityOrdersTbs.stream().forEach(i -> dateGenerator.generateDate(i));
            Map<Integer, List<CommodityOrdersTb>> commodityDateMap = commodityOrdersTbs.stream().collect(Collectors.groupingBy(CommodityOrdersTb::getDate));

            Map<Integer, List<CommodityOrdersTb>> resMap = commodityDateMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, // 保留原来的键
                    entry -> IntStream.range(0, entry.getValue().size()) // 获取索引范围
                            .filter(i -> i % 2 == 0 || i % 5 == 0) // 过滤出索引为 3 的倍数
                            .mapToObj(entry.getValue()::get) // 获取对应的订单对象
                            .collect(Collectors.toList()) // 收集为列表
            ));


            Map<Integer, CAmount> cAmountMap = new HashMap<>();

            List<CommodityOrdersTb> allCommodityOrdersTbList = new ArrayList<>();
            List<OrdersTb> allOrders = new ArrayList<>();

            for (int date : resMap.keySet()) {
                List<CommodityOrdersTb> commodityOrdersTbList = resMap.get(date);

                for (CommodityOrdersTb commodityOrderTb : commodityOrdersTbList) {
                    QueryWrapper<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbQueryWrapper = new QueryWrapper<>();
                    commodityOrderProfitSharingTbQueryWrapper.in("site_id", siteIds)
                            .eq("order_id", commodityOrderTb.getOrderId());
                    List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = commodityOrderProfitSharingTbMapper.selectList(commodityOrderProfitSharingTbQueryWrapper);

                    if (CollectionUtils.isEmpty(commodityOrderProfitSharingTbs)) {
                        continue;
                    }
                    commodityOrderProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
                    DeliveryMethodType deliveryMethodType = DeliveryMethodType.from(commodityOrderProfitSharingTbs.get(0).getDeliveryMethod());

                    //结算额度
                    Long expireTime = commodityOrderTb.getVipExpiredAt() == 0 ? commodityOrderTb.getPrepaidExpiredAt() : commodityOrderTb.getVipExpiredAt();
                    if (expireTime == 0) {
                        expireTime = commodityOrderTb.getCreatedAt() + commodityOrderTb.getCouponDuration();
                    }

                    String commodityOrderId = null;
                    if (deliveryMethodType == DeliveryMethodType.VIP_TIME) {
                        commodityOrderId = commodityOrderTb.getOrderId();
                    }

                    if (deliveryMethodType == DeliveryMethodType.VIP_TIME) {
                        commodityOrderProfitSharingTbs.stream().forEach(i -> {
                            cAmountMap.computeIfAbsent(i.getDate(), k -> new CAmount()).getVipAmount().addAndGet(i.getRechargeAmount());
                        });
                    } else {
                        commodityOrderProfitSharingTbs.stream().forEach(i -> {
                            cAmountMap.computeIfAbsent(i.getDate(), k -> new CAmount()).getPreAmount().addAndGet(i.getRechargeAmount());
                        });
                    }

                    //结算完毕
                    List<OrdersTb> ordersTbs = selecter.fillOrders(commodityOrderTb, commodityOrderProfitSharingTbs, deliveryMethodType, expireTime, commodityOrderId);

                    if (CollectionUtils.isNotEmpty(ordersTbs)) {
                        allOrders.addAll(ordersTbs);
                        allCommodityOrdersTbList.add(commodityOrderTb);
                    }
                }
            }
            for (int date : cAmountMap.keySet()) {
                CAmount cAmount = cAmountMap.get(date);
                if (cAmount.getPreAmount().get() == 0 && cAmount.getPreAmount().get() == 0) {
                    continue;
                }
                UpdateWrapper<EnsureIncomeTb> ensureIncomeTbQueryWrapper = new UpdateWrapper<>();
                ensureIncomeTbQueryWrapper
                        .eq("site_id", siteId)
                        .eq("date", date)
                        .setSql(cAmount.getPreAmount().get() != 0, "prepaid_money = prepaid_money-" + cAmount.getPreAmount())
                        .setSql(cAmount.getVipAmount().get() != 0, "vip_money = vip_money -" + cAmount.getVipAmount());
                ensureIncomeTbMapper.update(null, ensureIncomeTbQueryWrapper);
            }
            if (CollectionUtils.isNotEmpty(allCommodityOrdersTbList)) {
                commodityOrdersTbMapper.deleteBatchIds(allCommodityOrdersTbList);
            }
            if (CollectionUtils.isNotEmpty(allOrders)) {
                ordersTbMapper.deleteBatchIds(allOrders);
            }
        }
        return null;
    }

    public String clean(int channelId, int startDate, int endDate) throws Exception {
        QueryWrapper<ChannelSiteTb> channelSiteTbQueryWrapper = new QueryWrapper<>();
        channelSiteTbQueryWrapper.eq("channel_id", channelId).isNull("deleted_at");
        List<ChannelSiteTb> channelSiteTbList = channelSiteTbMapper.selectList(channelSiteTbQueryWrapper);


        List<Integer> siteIds;
        if (CollectionUtils.isEmpty(channelSiteTbList)) {
            QueryWrapper<ChannelTb> channelTbQueryWrapper = new QueryWrapper<>();
            channelTbQueryWrapper.eq("id", channelId);
            ChannelTb channelTb = channelTbMapper.selectOne(channelTbQueryWrapper);
            List<String> siteids = Arrays.stream(channelTb.getSelfSite().split(",")).collect(Collectors.toList());
            QueryWrapper<SiteTb> siteTbQueryWrapper = new QueryWrapper<>();
            siteTbQueryWrapper.in("id", siteids);
            List<SiteTb> siteTbList = siteTbMapper.selectList(siteTbQueryWrapper);
            siteIds = siteTbList.stream().map(SiteTb::getId).collect(Collectors.toList());
        } else {
            siteIds = channelSiteTbList.stream().map(ChannelSiteTb::getSiteId).collect(Collectors.toList());
        }

        long dateTimeStart = SIMPLE_DATE_FORMAT.parse(startDate + "").getTime() / 1000;
        long dateTimeEnd = SIMPLE_DATE_FORMAT.parse(endDate + "").getTime() / 1000;

        for (int siteId : siteIds) {

        }
            return null;
    }



}
