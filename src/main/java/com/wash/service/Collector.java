package com.wash.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.cache.DateCache;
import com.wash.entity.DecData;
import com.wash.entity.Series;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.data.*;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.entity.u.UserTb;
import com.wash.mapper.*;
import com.wash.service.calculator.VenCalculator;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/4/25
 * @Description
 */
@Service
public class Collector {
    private static final Logger LOGGER = LoggerFactory.getLogger(Collector.class);
    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    @Autowired
    private VenCalculator venCalculator;

    @Autowired
    private PayTbMapper payTbMapper;
    @Autowired
    private VendorProfitSharingTbMapper vendorProfitSharingTbMapper;



    @Autowired
    private UserTbMapper userTbMapper;

    @Autowired
    private CommodityOrdersTbMapper commodityOrdersTbMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private CommodityOrderProfitSharingTbMapper commodityOrderProfitSharingTbMapper;


    @Autowired
    private DateGenerator dateGenerator;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private List<PayTb> getPayTbsByDate(String selectedDate, FranchiseeSiteTb franchiseeSiteTb) throws Throwable {
        QueryWrapper<PayTb> payTbQueryWrapper = new QueryWrapper();
        long dateTimeStart = 0;
        try {
            dateTimeStart = SIMPLE_DATE_FORMAT.parse(selectedDate).getTime() / 1000;
        } catch (Exception e) {
            throw new RuntimeException(selectedDate, e);
        }
        long dateTimeEnd = dateTimeStart + 24 * 60 * 60;
        payTbQueryWrapper.eq("status", 1)
                .ge("created_at", dateTimeStart)
                .le("created_at", dateTimeEnd)
                .eq("site_id", franchiseeSiteTb.getSiteId());

        List<PayTb> payTbList = payTbMapper.selectList(payTbQueryWrapper);
        payTbList.stream().forEach(i -> dateGenerator.generateDate(i));
        if(CollectionUtils.isNotEmpty(payTbList)){
            return payTbList.stream().filter(i->i.getRefund()==0).collect(Collectors.toList());
        }else{
            return new ArrayList<>();
        }
    }

    public  List<Series> buildSeries(int size, FaSettlementTb faSettlementTb, FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, Integer inputDecAmount) throws Throwable {
        //STEP1:获取选定日期的pay
        List<PayTb> payTbList = getPayTbsByDate(faSettlementTb.getDate() + "", franchiseeSiteTb);
        List<Series> originSeries = genOriginSeries(payTbList, franchiseeSiteTb);

        List<Series> seriesList = null;
        DoubleSummaryStatistics stats = payTbList.stream()
                .collect(Collectors.summarizingDouble(PayTb::getAmount));
        double sum = stats.getSum();

        DecData decData = venCalculator.calculateAmount(inputVendorId,sum, inputDecAmount);
        if(decData==null){
            return null;
        }
        if(decData.getDecAmount()*3>sum){

            return null;
        }
        if (CollectionUtils.isEmpty(payTbList)) {
            return null;
        }
//        if ((decData.getSum() > 100000 || payTbList.size() > 15) && inputVendorId.intValue() != 3287) {
//            List<Series> list = filterSeriesByAmount(originSeries, decData);
//            seriesList = collectSeries(list, franchiseeSiteTb, inputVendorId, inputDecAmount);
//        } else {
        List<Series> list =collectSeries(originSeries, franchiseeSiteTb, inputVendorId, inputDecAmount);
        if(CollectionUtils.isEmpty(list)&&sum>120*100){
            LOGGER.info("未收集到：res,{},{},{},{},{}",inputVendorId,franchiseeSiteTb.getSiteId(),faSettlementTb.getDate(),decData.getDecAmount(),decData.getSum());
        }
        seriesList = filterSeriesByAmount(list, decData);

        return seriesList;
    }

    private List<Series> filterSeriesByAmount(List<Series> seriesList, DecData decData) {
        int tempAmount = 0;
        List<Series> resSeries = new ArrayList<>();

        Set<Integer> set = new HashSet<>();
        for (int i = 4; i > 1; i--) {
            Iterator<Series> iterator = seriesList.iterator();
            int k = 0;
            while (iterator.hasNext()) {
                Series series = iterator.next();
                if (series.getPayTb().getAmount() > 2 * decData.getSum() && !decData.isInputDec()) {
                    k++;
                    continue;
                }
                if (decData.isInputDec() && series.getPayTb().getAmount() > 2 * decData.getDecAmount()) {
                    k++;
                    continue;
                }
                if (set.contains(series.getPayTb().getId())) {
                    k++;
                    continue;
                }
                if (k % i == 0) {
                    int diff = tempAmount + series.getPayTb().getAmount() - decData.getDecAmount();
                    if (diff > 1000) {
                        int maxDiff = calMaxDiff(decData);
                        if (diff > maxDiff) {
                            k++;
                            continue;
                        }
                    }
                    resSeries.add(series);
                    tempAmount += series.getPayTb().getAmount();
                    set.add(series.getPayTb().getId());
                    if (tempAmount > decData.getDecAmount() - 500) {
                        return resSeries;
                    }
                }
                k++;
            }
        }
        seriesList.sort(Comparator.comparingInt(a -> a.getPayTb().getAmount()));
        if(tempAmount<decData.getDecAmount()-2000){
            for (Series series : seriesList) {
                if (set.contains(series.getPayTb().getId())) {
                    continue;
                }
                tempAmount+=series.getPayTb().getAmount();
                if(tempAmount>decData.getDecAmount()+1000){
                    return  resSeries;
                }
                resSeries.add(series);
            }
        }

        return resSeries;
    }

    //计算最大冗余
    private static int calMaxDiff(DecData decData) {
        int maxDiff = 0;
        if (decData.getDecAmount() / 5000 == 0) {
            maxDiff = 1500;
        } else if (decData.getDecAmount() / 5000 == 1) {
            maxDiff = 2600;
        } else if (decData.getDecAmount() / 5000 == 2) {
            maxDiff = 3200;
        } else if (decData.getDecAmount() / 5000 == 3) {
            maxDiff = 4600;
        } else if (decData.getDecAmount() / 5000 == 4) {
            maxDiff = 5800;
        } else if (decData.getDecAmount() / 5000 == 5) {
            maxDiff = 6900;
        } else if (decData.getDecAmount() / 5000 == 6) {
            maxDiff = 8500;
        } else {
            maxDiff = 9500;
        }
        return maxDiff;
    }

    private List<Series> genOriginSeries(List<PayTb> payTbList, FranchiseeSiteTb franchiseeSiteTb) {
        return payTbList.stream().map(i -> new Series(i, franchiseeSiteTb)).collect(Collectors.toList());
    }


    public List<Series> collectSeries(List<Series> originSeries, FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, Integer inputDecAmount) throws Throwable {
        if (originSeries == null) {//如果输入了金额 那就不管有几个单子了
            if (inputDecAmount == null) {
                return null;
            }
        }
        List<Series> seriesList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(originSeries.size());

        for (Series series : originSeries) {
            threadPoolExecutor.execute(() -> {
                try {
                    fillSeriesList(franchiseeSiteTb, inputVendorId, seriesList, series, countDownLatch);
                } catch (Throwable e) {
                    LOGGER.error("fillException:{}", ExceptionUtils.getStackTrace(e));
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        return seriesList;

    }

    private void fillSeriesList(FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, List<Series> seriesList, Series series, CountDownLatch countDownLatch) throws Throwable {
        try {
            PayTb payTb = series.getPayTb();
            CommodityOrdersTb commodityOrderTb = fillCO(series, payTb);
            if (commodityOrderTb == null) {
                countDownLatch.countDown();
                return;
            }
            UserTb userTb=fillUser(series,payTb);
            if(userTb==null){
                countDownLatch.countDown();
                return;
            }
            List<PayTb> userPays=fillUserPay(series,payTb);

            List<PayTb> others=userPays.stream().filter(i->i.getCreatedAt()!=payTb.getCreatedAt()).collect(Collectors.toList());
            //如果后续没有且用户最近没用，会导致该用户被清空，如果长期不用就无所谓。
            if(CollectionUtils.isEmpty(others)&&(System.currentTimeMillis()/1000)-payTb.getCreatedAt()<24*60*60*178){
                countDownLatch.countDown();
                return;
            }

            if(userPays.size()==1){
                UpdateWrapper<UserTb> userTbUpdateWrapper=new UpdateWrapper<>();
                userTbUpdateWrapper.eq("id",userTb.getId())
                        .set("activeness",0);
                userTbMapper.update(null,userTbUpdateWrapper);
            }


            QueryWrapper<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbQueryWrapper = new QueryWrapper<>();
            commodityOrderProfitSharingTbQueryWrapper
                    .eq("order_id", commodityOrderTb.getOrderId());
            List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = commodityOrderProfitSharingTbMapper.selectList(commodityOrderProfitSharingTbQueryWrapper);
            if (commodityOrderProfitSharingTbs == null || commodityOrderProfitSharingTbs.size() == 0) {
                countDownLatch.countDown();
                return;
            }
            commodityOrderProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
            series.setCommodityOrderProfitSharingTbs(commodityOrderProfitSharingTbs);
            DeliveryMethodType deliveryMethodType = DeliveryMethodType.from(commodityOrderProfitSharingTbs.get(0).getDeliveryMethod());
            if (!filleVpf(inputVendorId, series, payTb, commodityOrderProfitSharingTbs)) {
                countDownLatch.countDown();
                return;
            }

            //结算额度
            DoubleSummaryStatistics orderProfit = commodityOrderProfitSharingTbs.stream().collect(Collectors.summarizingDouble(CommodityOrderProfitSharingTb::getRechargeAmount));
            Long expireTime = commodityOrderTb.getVipExpiredAt() == 0 ? commodityOrderTb.getPrepaidExpiredAt() : commodityOrderTb.getVipExpiredAt();
            if (expireTime == 0) {
                expireTime = commodityOrderTb.getCreatedAt() + commodityOrderTb.getCouponDuration();
            }

            String commodityOrderId = null;
            if (deliveryMethodType == DeliveryMethodType.VIP_TIME) {
                commodityOrderId = commodityOrderTb.getOrderId();
            }
            //结算完毕
            if (orderProfit.getSum() == payTb.getAmount().intValue() || expireTime <= System.currentTimeMillis() / 1000) {
                List<OrdersTb> ordersTbs = fillOrders(commodityOrderTb, commodityOrderProfitSharingTbs, deliveryMethodType,
                        expireTime, commodityOrderId);
                if (CollectionUtils.isNotEmpty(ordersTbs)) {
                    series.setOrdersTbs(ordersTbs);
                    seriesList.add(series);
                }
            }
            countDownLatch.countDown();
        } catch (Exception e) {
            LOGGER.error("{},e->{}", series, ExceptionUtils.getStackTrace(e));
            throw new Throwable(ExceptionUtils.getStackTrace(e));
        }
    }

    private List<PayTb> fillUserPay(Series series, PayTb payTb) {
        QueryWrapper<PayTb> userPayTbQueryWrapper = new QueryWrapper<>();
        userPayTbQueryWrapper.eq("uid", payTb.getUid())
        .eq("status", 1);

        List<PayTb> userPayTbs = payTbMapper.selectList(userPayTbQueryWrapper);
        if (userPayTbs == null || userPayTbs.size() == 0) {
            return null;
        }
        userPayTbs.stream().forEach(i -> dateGenerator.generateDate(i));
        userPayTbs.sort((a,b)-> (int) (b.getCreatedAt()-a.getCreatedAt()));
        series.setUPaytbs(userPayTbs);
        return userPayTbs;
    }

    private UserTb fillUser(Series series, PayTb payTb) {
        QueryWrapper<UserTb> userTbQueryWrapper = new QueryWrapper<>();
        userTbQueryWrapper.eq("id", payTb.getUid());
        List<UserTb> userTbs = userTbMapper.selectList(userTbQueryWrapper);
        if (userTbs == null || userTbs.size() == 0) {
            return null;
        }
        UserTb userTb = userTbs.get(0);
        series.setUserTb(userTb);
        dateGenerator.generateDate(userTb);
        return userTb;
    }

    public  List<OrdersTb> fillOrders(CommodityOrdersTb commodityOrderTb,
                                      List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs,
                                      DeliveryMethodType deliveryMethodType, Long expireTime,
                                      String commodityOrderId) {
        QueryWrapper<OrdersTb> ordersTbQueryWrapper = new QueryWrapper<>();
        //这里不能有site
        ordersTbQueryWrapper.eq("uid", commodityOrderTb.getUid())
                .ge("created_at", commodityOrderTb.getCreatedAt())
                .eq(StringUtils.isNotEmpty(commodityOrderId), "commodity_order_id", commodityOrderId);
        List<OrdersTb> ordersTbs = ordersTbMapper.selectList(ordersTbQueryWrapper);
        if(CollectionUtils.isEmpty(ordersTbs)){
            return null;
        }
        ordersTbs.sort((a,b)-> (int) (b.getCreatedAt()-a.getCreatedAt()));

        final long exp = expireTime;
        List<OrdersTb> lastOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ordersTbs)) {
            lastOrders = ordersTbs.stream().filter(i -> i.getCreatedAt() > exp).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(lastOrders)) {
            long lastMonthTime = System.currentTimeMillis() / 1000 - 30 * 24 * 60 * 60;
            boolean exists = lastOrders.stream().anyMatch(i -> i.getCreatedAt() > lastMonthTime);
            if (exists) {
                return null;
            }
        }


        long start=commodityOrderProfitSharingTbs.get(0).getCreatedAt()-90*60;
        long end=commodityOrderProfitSharingTbs.get(commodityOrderProfitSharingTbs.size()-1).getCreatedAt()+15*60;

        List<OrdersTb> resOrdersTbs = new ArrayList<>();
        //次卡是否都是一次？
        if (ordersTbs != null && ordersTbs.size() > 0) {
            if (deliveryMethodType == DeliveryMethodType.COUPON_WASHING || deliveryMethodType == DeliveryMethodType.PER_USE_CARD) {
                resOrdersTbs.add(ordersTbs.get(0));
            } else if (deliveryMethodType == DeliveryMethodType.PREPAID) {
               List<OrdersTb> preOrders= ordersTbs.stream().filter(i -> i.getCreatedAt() >= start && i.getCreatedAt() <= end)
                       .collect(Collectors.toList());
               resOrdersTbs.addAll(preOrders);
            } else if (deliveryMethodType == DeliveryMethodType.VIP_TIME || deliveryMethodType == DeliveryMethodType.PREPAID_SUIT) {
                List<OrdersTb> vipOrders = ordersTbs.stream().filter(i -> i.getCreatedAt() <= exp).collect(Collectors.toList());
                resOrdersTbs.addAll(vipOrders);
            }
        }

        resOrdersTbs.stream().forEach(i -> dateGenerator.generateDate(i));
        return resOrdersTbs;
    }

    private CommodityOrdersTb fillCO(Series series, PayTb payTb) {
        QueryWrapper<CommodityOrdersTb> commodityOrderTbQueryWrapper = new QueryWrapper<>();
        commodityOrderTbQueryWrapper.eq("pay_sn", payTb.getPaySn()).eq("site_id", payTb.getSiteId());
        List<CommodityOrdersTb> commodityOrderTbs = commodityOrdersTbMapper.selectList(commodityOrderTbQueryWrapper);
        if (commodityOrderTbs == null || commodityOrderTbs.size() == 0) {
            return null;
        }
        CommodityOrdersTb commodityOrderTb = commodityOrderTbs.get(0);
        series.setCommodityOrderTb(commodityOrderTb);
        dateGenerator.generateDate(commodityOrderTb);
        return commodityOrderTb;
    }

    private boolean filleVpf( Integer inputVendorId, Series series, PayTb payTb, List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs) {
        QueryWrapper<VendorProfitSharingTb> vendorProfitSharingTbQueryWrapper = new QueryWrapper<>();
        //不能限制场，也不能type，跨会有负
        vendorProfitSharingTbQueryWrapper
                .in("transaction_id", commodityOrderProfitSharingTbs.stream().map(i -> i.getTransactionId()).collect(Collectors.toList()));

        List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(vendorProfitSharingTbQueryWrapper);
        if (vendorProfitSharingTbs == null || vendorProfitSharingTbs.size() == 0) {
            return false;
        }
        vendorProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
        List<VendorProfitSharingTb> owns=vendorProfitSharingTbs.stream().filter(i->i.getVendorId().intValue()==inputVendorId.intValue()).collect(Collectors.toList());
        List<VendorProfitSharingTb> parents=vendorProfitSharingTbs.stream().filter(i->i.getVendorId().intValue()!=inputVendorId.intValue()).collect(Collectors.toList());
        series.setVendorProfitSharingTbs(owns);

        if(CollectionUtils.isNotEmpty(parents)) {
            series.setParentVendorProfitSharingTbs(parents);
            series.setParentVen(parents.get(0).getVendorId());
        }
        return true;//填充成功
    }

}
