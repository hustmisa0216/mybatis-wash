package com.wash.service.channel;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.CommodityOrderProfitSharingTb;
import com.wash.entity.data.CommodityOrdersTb;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.PayTb;
import com.wash.entity.site.SiteTb;
import com.wash.entity.statistics.EnsureIncomeTb;
import com.wash.mapper.*;
import com.wash.mapper.channel.ChannelSiteTbMapper;
import com.wash.mapper.channel.ChannelTbMapper;
import com.wash.service.Collector;
import com.wash.service.channel.entity.CAmount;
import com.wash.service.channel.entity.ChannelSiteTb;
import com.wash.service.channel.entity.ChannelTb;
import com.wash.service.channel.entity.OrderEntity;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static  SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    public  static final String FILE_PATH = "D:\\mogo\\channel\\";

    @Autowired
    private ChannelSiteTbMapper channelSiteTbMapper;

    @Autowired
    private PayTbMapper payTbMapper;
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
    private Collector collector;

    public String start(int channelId, int startDate, int endDate) throws ParseException, IOException {
        QueryWrapper<ChannelSiteTb> channelSiteTbQueryWrapper = new QueryWrapper<>();
        channelSiteTbQueryWrapper.eq("channel_id", channelId).isNull("deleted_at");
        List<ChannelSiteTb> channelSiteTbList = channelSiteTbMapper.selectList(channelSiteTbQueryWrapper);
        // 构建目标目录路径
        String dirPath = FILE_PATH + startDate + "-" + endDate;
        File dir = new File(dirPath);
        // 检查目录是否存在，不存在则创建
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("无法创建目录: " + dirPath);
            }
        }
        FileWriter commoOrderWriter = new FileWriter(FILE_PATH +startDate+"-"+endDate+"\\"+ FilesEnum.COMMODITY_ORDER_DATA.getFileName(), true);
        FileWriter orderWriter = new FileWriter(FILE_PATH +startDate+"-"+endDate+"\\"+ FilesEnum.ORDERSTB_DATA.getFileName(), true);
        FileWriter paywriter = new FileWriter(FILE_PATH +startDate+"-"+endDate+"\\"+ FilesEnum.PAYTB_DATA.getFileName(), true);

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
            List<CommodityOrdersTb> commodityOrders = commodityOrdersTbMapper.selectList(commodityOrdersTbQueryWrapper);

            List<OrderEntity> res=filterCommodityOrdersTb(commodityOrders,siteIds);

            res.stream().forEach(i -> dateGenerator.generateDate(i.getCommodityOrdersTb()));
            Map<Integer, List<OrderEntity>> commodityDateMap = res.stream().collect(Collectors.groupingBy(orderEntity -> orderEntity.getCommodityOrdersTb().getDate()));

            Map<Integer, List<OrderEntity>> resMap = commodityDateMap.entrySet()
                    .stream().collect(Collectors.toMap(Map.Entry::getKey, // 保留原来的键
                    entry -> IntStream.range(0, entry.getValue().size()) // 获取索引范围
                            .filter(i -> i % 2== 0||i%3==0) // 过滤出索引为 3 的倍数
                            .mapToObj(entry.getValue()::get) // 获取对应的订单对象
                            .collect(Collectors.toList()) // 收集为列表
            ));

            Map<Integer, CAmount> cAmountMap = new HashMap<>();

            List<CommodityOrdersTb> allCommodityOrdersTbList = new ArrayList<>();
            List<OrdersTb> allOrders = new ArrayList<>();

            List<PayTb> payTbs=new ArrayList<>();
            for (int date : resMap.keySet()) {
                List<OrderEntity> orderEntities = resMap.get(date);

                for (OrderEntity orderEntity : orderEntities) {
                    allCommodityOrdersTbList.add(orderEntity.getCommodityOrdersTb());
                    allOrders.addAll(orderEntity.getOrdersTbs());
                    payTbs.add(orderEntity.getPayTb());
                    List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = orderEntity.getCommodityOrderProfitSharingTbs();
                    DeliveryMethodType deliveryMethodType = DeliveryMethodType.from(commodityOrderProfitSharingTbs.get(0).getDeliveryMethod());
                    if (deliveryMethodType == DeliveryMethodType.VIP_TIME) {
                        commodityOrderProfitSharingTbs.stream().forEach(i -> cAmountMap.computeIfAbsent(i.getDate(), k -> new CAmount())
                                .getVipAmount().addAndGet(i.getRechargeAmount()));
                    } else {
                        commodityOrderProfitSharingTbs.stream().forEach(i -> cAmountMap.computeIfAbsent(i.getDate(), k -> new CAmount())
                                .getPreAmount().addAndGet(i.getRechargeAmount()));
                    }
                }
            }

            Iterator<Map.Entry<Integer, CAmount>> iterator = cAmountMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, CAmount> entry = iterator.next();
                int date = entry.getKey();
                CAmount cAmount = entry.getValue();
                if (cAmount.getPreAmount().get() == 0 && cAmount.getPreAmount().get() == 0) {
                    continue;
                }
                QueryWrapper<EnsureIncomeTb> queryWrapper = new QueryWrapper<>();
                queryWrapper
                        .eq("site_id", siteId)
                        .eq("date", date);
                List<EnsureIncomeTb> ensureIncomeTbs=ensureIncomeTbMapper.selectList(queryWrapper);
                if(CollectionUtils.isEmpty(ensureIncomeTbs)){
                   iterator.remove();
                }else{
                    double sum=ensureIncomeTbs.stream().mapToDouble(EnsureIncomeTb::getPrepaidMoney).sum();
                    double sumvip=ensureIncomeTbs.stream().mapToDouble(EnsureIncomeTb::getVipMoney).sum();
                    if(sum<cAmount.getPreAmount().get()){
                        iterator.remove();
                        continue;
                    }
                    if(sumvip<cAmount.getVipAmount().get()){
                        iterator.remove();
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
                allCommodityOrdersTbList.stream().forEach(i -> {
                    try {
                        commoOrderWriter.write(i.toString() + "\n");
                        commoOrderWriter.flush();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                commodityOrdersTbMapper.deleteBatchIds(allCommodityOrdersTbList);
            }

            if (CollectionUtils.isNotEmpty(allOrders)) {
                allOrders.stream().forEach(i -> {
                    try {
                        orderWriter.write(i.toString() + "\n");
                        orderWriter.flush();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                ordersTbMapper.deleteBatchIds(allOrders);
            }
            if (CollectionUtils.isNotEmpty(payTbs)) {
                allOrders.stream().forEach(i -> {
                    try {
                        paywriter.write(i.toString() + "\n");
                        paywriter.flush();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                payTbMapper.deleteBatchIds(payTbs);
            }
        }
        return null;
    }

    private List<OrderEntity> filterCommodityOrdersTb(List<CommodityOrdersTb> commodityOrdersTbs, List<Integer> siteIds) {

        List<OrderEntity> res=new ArrayList<>();
        for(CommodityOrdersTb commodityOrderTb: commodityOrdersTbs) {
            QueryWrapper<PayTb> payTbQueryWrapper = new QueryWrapper<>();
            payTbQueryWrapper.in("site_id", siteIds)
                    .eq("pay_sn", commodityOrderTb.getPaySn());
            List<PayTb> payTbs=payTbMapper.selectList(payTbQueryWrapper);

            if (CollectionUtils.isEmpty(payTbs)) {
                continue;
            }
            QueryWrapper<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbQueryWrapper = new QueryWrapper<>();
            commodityOrderProfitSharingTbQueryWrapper.in("site_id", siteIds)
                    .eq("order_id", commodityOrderTb.getOrderId());
            List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = commodityOrderProfitSharingTbMapper.selectList(commodityOrderProfitSharingTbQueryWrapper);

            if (CollectionUtils.isEmpty(commodityOrderProfitSharingTbs)) {
                continue;
            }

            double sum = commodityOrderProfitSharingTbs.stream().mapToDouble(CommodityOrderProfitSharingTb::getRechargeAmount).sum();


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

            if (commodityOrderTb.getPaymentMoney().intValue() == sum || expireTime < System.currentTimeMillis() / 1000 - 60 * 60 * 24 * 9) {
                List<OrdersTb> ordersTbs = collector.fillOrders(commodityOrderTb, commodityOrderProfitSharingTbs, deliveryMethodType, expireTime, commodityOrderId);
                if (CollectionUtils.isNotEmpty(ordersTbs)) {
                    res.add(new OrderEntity(commodityOrderTb, commodityOrderProfitSharingTbs, ordersTbs,payTbs.get(0)));
                }
            }
        }
            return res;
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
