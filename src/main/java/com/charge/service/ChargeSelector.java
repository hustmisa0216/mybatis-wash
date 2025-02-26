package com.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.charge.entity.SiteLatestData;
import com.charge.entity.SiteVendor;
import com.charge.entity.StatementDaily;
import com.charge.mapper.SiteLatestDataMapper;
import com.charge.mapper.SiteVendorMapper;
import com.charge.mapper.StatementDailyMapper;
import com.wash.cache.DateCache;
import com.wash.entity.*;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.*;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.entity.statistics.DailyPaperTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.entity.statistics.SiteLatestDataTb;
import com.wash.mapper.*;
import com.wash.service.Modifier;
import com.wash.service.Recorder;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.wash.service.Recorder.buildFileFolder;

@Component
public class ChargeSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeSelector.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private Recorder recorder;

    @Autowired
    private Modifier modifier;

    @Autowired
    private SiteVendorMapper siteVendorMapper;

    @Autowired
    private StatementDailyMapper statementDailyMapper;
    @Autowired
    private PayTbMapper payTbMapper;
    @Autowired
    private VendorProfitSharingTbMapper vendorProfitSharingTbMapper;

    @Autowired
    private FaSettlementTbMapper faSettlementTbMapper;

    @Autowired
    private CommodityOrdersTbMapper commodityOrdersTbMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private CommodityOrderProfitSharingTbMapper commodityOrderProfitSharingTbMapper;
    @Autowired
    private SiteLatestDataMapper siteLatestDataTbTbMapper;

    @Autowired
    private DateCache dateCache;

    @Autowired
    private DateGenerator dateGenerator;

    private static Calendar calendar = Calendar.getInstance();
    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;

    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();


    public String select(Integer inputVendorId, Integer inputSiteId, Integer inputDate, Integer inputDecAmount) throws InterruptedException {

        //STEP0 获取vendor 场地
        List<SiteVendor> siteVendors = getFranchiseeSiteTbs(inputVendorId);
        if (CollectionUtils.isEmpty(siteVendors)) return "未获取到当前franchise";
        List<Integer> siteIds=siteVendors.stream().map(i->i.getSiteId()).collect(Collectors.toList());

        List<SiteLatestData> siteLatestData = getSiteLastDatas(siteIds);

        double vendorIncome = siteLatestData.stream().mapToDouble(i -> i.getChargeConsumeAmount()).sum();

        Integer selectDate = selectDate(inputVendorId, siteIds, vendorIncome);


        StringBuffer res = new StringBuffer();
        FranchiseeTb franchiseeTb = franchiseeTbMapper.selectById(inputVendorId);
        //每个场地单独处理

        return res.toString();
    }

    private Integer  selectDate(Integer inputVendorId, List<Integer> siteIds, double vendorIncome) {

        QueryWrapper<StatementDaily> statementDailyQueryWrapper = new QueryWrapper();
        long lastDateTime = (System.currentTimeMillis() / 1000) - 25 * 24 * 60 * 60;
        int lastDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(lastDateTime * 1000)));
        long firstTime = System.currentTimeMillis() / 1000 - 540 * 24 * 60 * 60;
        int firstDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(firstTime * 1000)));

        statementDailyQueryWrapper.in("site_id",siteIds)
                .ge("date", firstDate)
                .le("date", lastDate);

        List<StatementDaily> statementDailies=statementDailyMapper.selectList(statementDailyQueryWrapper);
        TreeMap<Integer, List<StatementDaily>> dateMap = statementDailies.stream()
                .collect(Collectors.toMap(
                        StatementDaily::getDate,
                        statementDaily -> {
                            List<StatementDaily> list = new ArrayList<>();
                            list.add(statementDaily);
                            return list;
                        },
                        (existing, replacement) -> {
                            existing.addAll(replacement);
                            return existing;
                        },
                        TreeMap::new // 使用 TreeMap
                ));
        for(int date:dateMap.keySet()){
            double chargeSum=dateMap.get(date).stream().mapToDouble(i->i.getChargeConsumeAmount()).sum();
            if(Math.abs(chargeSum-vendorIncome*2)<3000){

                return date;
            }
        }
        return null;

    }

    private List<SiteLatestData> getSiteLastDatas(List<Integer> siteIds) {
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String sDate = "";
        if (hour < 6) {
            sDate = SIMPLE_DATE_FORMAT.format(new Date(time - 24 * 60 * 60 * 1000));//如果是凌晨需要取前一天的日期
        } else {
            sDate = SIMPLE_DATE_FORMAT.format(new Date(time));
        }
        QueryWrapper<SiteLatestData> siteLatestDataTbQueryWrapper = new QueryWrapper<>();
        int index=hour*12;
        siteLatestDataTbQueryWrapper.in("site_id",siteIds)
                .eq("date",sDate)
                .eq("num_index",index);
        List<SiteLatestData> siteLatestDatas=siteLatestDataTbTbMapper.selectList(siteLatestDataTbQueryWrapper);
        return siteLatestDatas;
    }


    private void updateFranchisee(Integer inputVendorId, FranchiseeSiteTb franchiseeSiteTb, ModifierData modifierData) {
        UpdateWrapper<FranchiseeTb> franchiseeTbUpdateWrapper = new UpdateWrapper<>();
        franchiseeTbUpdateWrapper.eq("id", inputVendorId)
                .setSql("settled_amount = settled_amount-" + modifierData.getTotalIncome())
                .setSql("wait_withdraw = wait_withdraw-" + modifierData.getTotalIncome())
                .setSql("stmt_recharge_amount = stmt_recharge_amount-" + modifierData.getTotalChargeAmount())
                .setSql("stmt_profit_amount = stmt_profit_amount-" + modifierData.getTotalChargeAmount());
        franchiseeTbMapper.update(null, franchiseeTbUpdateWrapper);

        if (modifierData.getParentTotalIncome() > 0) {
            UpdateWrapper<FranchiseeTb> franchiseeTbUpdateWrapperParent = new UpdateWrapper<>();
            franchiseeTbUpdateWrapperParent.eq("id", franchiseeSiteTb.getParentId())
                    .setSql("settled_amount = settled_amount-" + modifierData.getParentTotalIncome())
                    .setSql("wait_withdraw = wait_withdraw-" + modifierData.getParentTotalIncome())
                    .setSql("stmt_recharge_amount = stmt_recharge_amount-" + modifierData.getTotalChargeAmount())
                    .setSql("stmt_profit_amount = stmt_profit_amount-" + modifierData.getTotalChargeAmount());
            franchiseeTbMapper.update(null, franchiseeTbUpdateWrapperParent);
        }
    }

    private void record(Integer inputVendorId, FranchiseeSiteTb franchiseeSiteTb, ModifierData modifierData, DailyData dailyData) throws Exception {
        FranchiseeTb after = franchiseeTbMapper.selectById(inputVendorId);
        modifierData.setAfterWaitDraw(after.getWaitWithdraw());
        recorder.record(inputVendorId, dailyData.getFaSettlementTb(), franchiseeSiteTb, modifierData);

        String path = buildFileFolder(inputVendorId, franchiseeSiteTb.getSiteId(), dailyData.getFaSettlementTb().getDate());
        FileWriter dateWriter = new FileWriter(path + FilesEnum.DATE.getFileName(), true);
        dateWriter.write(modifierData.getKey());
        dateWriter.flush();
    }

    @Transactional(rollbackFor = Exception.class)
        // 所有异常均触发回滚
    ModifierData updateAndDel(Integer inputVendorId, FranchiseeSiteTb franchiseeSiteTb, DailyData dailyData,
                              List<Series> resSeries, FranchiseeTb franchiseeTb) throws Exception {
        modifier.delete(resSeries);
        ModifierData modifierData = modifier.update(franchiseeTb, inputVendorId, dailyData, franchiseeSiteTb, resSeries);
        return modifierData;
    }



    private FaSettlementTb getFaSettlementTb(Integer inputVendorId, Integer siteId, Integer inputDate) {

        List<FaSettlementTb> faSettlementTbs = faSettlementTbMapper.selectList(
                new QueryWrapper<FaSettlementTb>()
                        .eq("date", inputDate)
                        .eq("own_id", inputVendorId)
                        .eq("site_id", siteId));

        if (CollectionUtils.isNotEmpty(faSettlementTbs)) {
            return faSettlementTbs.get(0);
        }
        return null;
    }

    private List<Series> buildSeries(FaSettlementTb faSettlementTb, FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, Integer inputDecAmount) throws Throwable {
        //STEP1:获取选定日期的pay
        List<PayTb> payTbList = getPayTbsByDate(faSettlementTb.getDate() + "", franchiseeSiteTb);
        List<Series> originSeries = genOriginSeries(payTbList, franchiseeSiteTb);

        List<Series> seriesList = null;
        DecData decData = calculateAmount(payTbList, inputDecAmount);

        if (CollectionUtils.isEmpty(payTbList)) {
            return null;
        }
        if ((decData.getSum() > 32000 || payTbList.size() > 11) && inputVendorId.intValue() != 3287) {
            List<Series> list = filterSeriesByAmount(originSeries, decData);
            seriesList = collectSeries(list, franchiseeSiteTb, inputVendorId, inputDecAmount);
        } else {
            List<Series> list = collectSeries(originSeries, franchiseeSiteTb, inputVendorId, inputDecAmount);
            seriesList = filterSeriesByAmount(list, decData);
        }
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
        return resSeries;
    }

    //计算最大冗余
    private static int calMaxDiff(DecData decData) {
        int maxDiff = 0;
        if (decData.getDecAmount() / 10000 == 0) {
            maxDiff = 3000;
        } else if (decData.getDecAmount() / 10000 == 1) {
            maxDiff = 5500;
        } else if (decData.getDecAmount() / 10000 == 2) {
            maxDiff = 8900;
        } else {
            maxDiff = 11900;
        }
        return maxDiff;
    }

    private List<Series> genOriginSeries(List<PayTb> payTbList, FranchiseeSiteTb franchiseeSiteTb) {
        return payTbList.stream().map(i -> new Series(i, franchiseeSiteTb)).collect(Collectors.toList());
    }

    //根据选定history 的计算额度
    public DecData calculateAmount(List<PayTb> payTbList, Integer inputDecAmount) {
        DoubleSummaryStatistics stats = payTbList.stream()
                .collect(Collectors.summarizingDouble(PayTb::getAmount));
        double sum = stats.getSum();
        double calAmount = 0;
        double calSum = sum / 100;
        if (calSum < 100) {
            calAmount = sum / 4;
        } else if (calSum < 200) {
            calAmount = sum / 6;
        } else if (calSum < 400) {
            calAmount = sum / 7;
        } else if (calSum < 600) {
            calAmount = sum / 8;
        } else if (calSum < 800) {
            calAmount = sum / 9;
        } else if (calSum < 2000) {
            calAmount = sum / 10;
        } else {
            calAmount = sum / 11;
        }
        int decAmount = inputDecAmount != null ? inputDecAmount : (int) calAmount;//程序内限制的amount,需要同事满足两个
        return new DecData(sum, decAmount, inputDecAmount != null);
    }

    private List<SiteVendor> getFranchiseeSiteTbs(Integer inputVendorId) {
        QueryWrapper<SiteVendor> franchiseeTbQueryWrapper = new QueryWrapper();
        franchiseeTbQueryWrapper.eq("vendor_id", inputVendorId);
        List<SiteVendor> franchiseeSiteTbs = siteVendorMapper.selectList(franchiseeTbQueryWrapper);
        return franchiseeSiteTbs;
    }

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
        return payTbList;
    }



    public boolean judgeExists(int inputVendorId, int siteId, int date) {
        Set<Integer> dateSet = new HashSet<>();
        if (dateCache.SITE_DATE_MAP.containsKey(inputVendorId)) {
            if (dateCache.SITE_DATE_MAP.get(inputVendorId).containsKey(siteId)) {
                dateSet = dateCache.SITE_DATE_MAP.get(inputVendorId).get(siteId);
            }
        }
        return dateSet.contains(date);
    }




    private List<Series> collectSeries(List<Series> originSeries, FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, Integer inputDecAmount) throws Throwable {
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


            QueryWrapper<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbQueryWrapper = new QueryWrapper<>();
            commodityOrderProfitSharingTbQueryWrapper.eq("site_id", payTb.getSiteId())
                    .eq("order_id", commodityOrderTb.getOrderId());
            List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = commodityOrderProfitSharingTbMapper.selectList(commodityOrderProfitSharingTbQueryWrapper);
            if (commodityOrderProfitSharingTbs == null || commodityOrderProfitSharingTbs.size() == 0) {
                countDownLatch.countDown();
                return;
            }
            commodityOrderProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
            series.setCommodityOrderProfitSharingTbs(commodityOrderProfitSharingTbs);
            DeliveryMethodType deliveryMethodType = DeliveryMethodType.from(commodityOrderProfitSharingTbs.get(0).getDeliveryMethod());
            if (!filleVpf(franchiseeSiteTb, inputVendorId, series, payTb, commodityOrderProfitSharingTbs)) {
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
            if (orderProfit.getSum() == payTb.getAmount() || expireTime <= System.currentTimeMillis() / 1000) {
                List<OrdersTb> ordersTbs = fillOrders(payTb, commodityOrderTb, commodityOrderProfitSharingTbs, deliveryMethodType,
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

    private List<OrdersTb> fillOrders(PayTb payTb, CommodityOrdersTb commodityOrderTb,
                                      List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs,
                                      DeliveryMethodType deliveryMethodType, Long expireTime,
                                      String commodityOrderId) {
        QueryWrapper<OrdersTb> ordersTbQueryWrapper = new QueryWrapper<>();
        ordersTbQueryWrapper.eq("uid", commodityOrderTb.getUid())
                .ge("created_at", commodityOrderTb.getCreatedAt())
                .eq("site_id", payTb.getSiteId())
                .eq(StringUtils.isNotEmpty(commodityOrderId), "commodity_order_id", commodityOrderId);
        List<OrdersTb> ordersTbs = ordersTbMapper.selectList(ordersTbQueryWrapper);

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

        List<OrdersTb> resOrdersTbs = new ArrayList<>();
        //次卡是否都是一次？
        if (ordersTbs != null && ordersTbs.size() > 0) {
            if (deliveryMethodType == DeliveryMethodType.COUPON_WASHING || deliveryMethodType == DeliveryMethodType.PER_USE_CARD) {
                resOrdersTbs.add(ordersTbs.get(0));
            } else if (deliveryMethodType == DeliveryMethodType.PREPAID) {
                if (ordersTbs.size() >= commodityOrderProfitSharingTbs.size()) {
                    resOrdersTbs.addAll(ordersTbs.subList(0, commodityOrderProfitSharingTbs.size()));
                } else {
                    resOrdersTbs.addAll(ordersTbs);
                }
            } else if (deliveryMethodType == DeliveryMethodType.VIP_TIME || deliveryMethodType == DeliveryMethodType.PREPAID_SUIT) {
                List<OrdersTb> vipOrders = ordersTbs.stream().filter(i -> i.getCreatedAt() <= exp).collect(Collectors.toList());
                resOrdersTbs.addAll(vipOrders);
            }
        }

        ordersTbs.stream().forEach(i -> dateGenerator.generateDate(i));
        return ordersTbs;
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

    private boolean filleVpf(FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, Series series, PayTb payTb, List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs) {
        QueryWrapper<VendorProfitSharingTb> vendorProfitSharingTbQueryWrapper = new QueryWrapper<>();
        vendorProfitSharingTbQueryWrapper.eq("site_id", payTb.getSiteId())
                .eq("type", 1)
                .eq("vendor_id", inputVendorId)
                .in("transaction_id", commodityOrderProfitSharingTbs.stream().map(i -> i.getTransactionId()).collect(Collectors.toList()));

        List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(vendorProfitSharingTbQueryWrapper);
        if (vendorProfitSharingTbs == null || vendorProfitSharingTbs.size() == 0) {
            return false;
        }
        vendorProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
        series.setVendorProfitSharingTbs(vendorProfitSharingTbs);

        if (franchiseeSiteTb.getParentId() != null && franchiseeSiteTb.getParentId() > 0 && franchiseeSiteTb.getParentPercent().doubleValue() > 0) {
            QueryWrapper<VendorProfitSharingTb> parentVendorProfitSharingTbQueryWrapper = new QueryWrapper<>();
            parentVendorProfitSharingTbQueryWrapper.eq("site_id", payTb.getSiteId())
                    .eq("type", 1)
                    .eq("vendor_id", franchiseeSiteTb.getParentId())
                    .in("transaction_id", commodityOrderProfitSharingTbs.stream().map(i -> i.getTransactionId()).collect(Collectors.toList()));
            List<VendorProfitSharingTb> parentVendorPtbs = vendorProfitSharingTbMapper.selectList(parentVendorProfitSharingTbQueryWrapper);
            parentVendorPtbs.stream().forEach(i -> dateGenerator.generateDate(i));
            series.setParentVendorProfitSharingTbs(parentVendorPtbs);
        }

        return true;//填充成功
    }

}
