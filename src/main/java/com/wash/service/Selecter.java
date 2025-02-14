package com.wash.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wash.cache.DateCache;
import com.wash.entity.DailyData;
import com.wash.entity.DecData;
import com.wash.entity.ModifierData;
import com.wash.entity.Series;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.*;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.statistics.DailyPaperTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.mapper.*;
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
import java.util.stream.Collectors;

import static com.wash.service.Recorder.buildFileFolder;

@Component
public class Selecter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Selecter.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private Recorder recorder;

    @Autowired
    private Modifier modifier;

    @Autowired
    private FranchiseeSiteTbMapper franchiseeSiteTbMapper;

    @Autowired
    private DailyPaperTbMapper dailyPaperTbMapper;
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
    private DateCache dateCache;

    @Autowired
    private DateGenerator dateGenerator;

    private static Calendar calendar = Calendar.getInstance();

    @Transactional
    public String select(Integer inputVendorId, Integer inputSiteId, Integer inputDate, Integer inputDecAmount) throws Throwable {

        //STEP0 获取vendor 场地
        List<FranchiseeSiteTb> franchiseeSiteTbs = getFranchiseeSiteTbs(inputVendorId);
        if (CollectionUtils.isEmpty(franchiseeSiteTbs)) return "未获取到当前franchise";
        StringBuffer res=new StringBuffer();

        //每个场地单独处理
        for (FranchiseeSiteTb franchiseeSiteTb : franchiseeSiteTbs) {
            if (inputSiteId != null) {
                if (franchiseeSiteTb.getSiteId().intValue() != inputSiteId.intValue()) continue;
            }
                DailyData dailyData = null;
                if (inputDate == null) {
                    DoubleSummaryStatistics todayVendorSum = getTodayIncome(franchiseeSiteTb, inputVendorId);
                    double amount=inputDecAmount==null?todayVendorSum.getSum():inputDecAmount.intValue()*8;
                    dailyData = selectHistoryDate(franchiseeSiteTb, amount, inputVendorId);
                } else {
                    if (judgeExists(inputVendorId, inputSiteId, inputDate)) {
                        return "该日期已经处理,请谨慎输入";
                    } else {
                       FaSettlementTb faSettlementTbRes = getFaSettlementTb(inputVendorId,franchiseeSiteTb.getSiteId(),inputDate);
                       DailyPaperTb dailyPaperTb=getDailyDataTb(inputVendorId,franchiseeSiteTb.getSiteId(),inputDate);
                       dailyData=new DailyData(dailyPaperTb,faSettlementTbRes);
                    }
                }
                if(inputSiteId==null){
                    if (dailyData == null) continue;
                }else{
                    if (dailyData == null)
                        return "未获取到当日收入";
                }

                List<Series> resSeries = buildSeries(dailyData.getFaSettlementTb(), franchiseeSiteTb, inputVendorId, inputDecAmount);

                if (CollectionUtils.isEmpty(resSeries)) {
                    return "未获取到任何条目";
                }
                Thread.sleep(200);

                modifier.delete(inputVendorId, dailyData.getFaSettlementTb(), franchiseeSiteTb, resSeries);
                ModifierData modifierData = modifier.update(inputVendorId, dailyData, franchiseeSiteTb, resSeries);
                recorder.record(inputVendorId, dailyData.getFaSettlementTb(), franchiseeSiteTb, modifierData);

                String path = buildFileFolder(inputVendorId, franchiseeSiteTb.getSiteId(), dailyData.getFaSettlementTb().getDate());
                FileWriter dateWriter = new FileWriter(path + FilesEnum.DATE.getFileName(), true);
                dateWriter.write(modifierData.getKey());
                dateWriter.flush();
                if(StringUtils.isNotBlank(res)){
                    res.append("\n");
                }
                res.append(modifierData.getKey());

        }
        return res.toString();
    }

    private DailyPaperTb getDailyDataTb(Integer inputVendorId, Integer siteId, Integer inputDate) {
        QueryWrapper<DailyPaperTb> dailyPaperTbQueryWrapper = new QueryWrapper();

        dailyPaperTbQueryWrapper
                .eq("site_id", siteId)
                .le("date",inputDate);
        return dailyPaperTbMapper.selectList(dailyPaperTbQueryWrapper).get(0);
    }

    private FaSettlementTb getFaSettlementTb(Integer inputVendorId,Integer siteId,Integer inputDate) {

        List<FaSettlementTb> faSettlementTbs = faSettlementTbMapper.selectList(
                new QueryWrapper<FaSettlementTb>()
                        .eq("date", inputDate)
                        .eq("own_id", inputVendorId)
                        .eq("site_id", siteId));

        if (CollectionUtils.isNotEmpty(faSettlementTbs)) {
            return  faSettlementTbs.get(0);
        }
        return null;
    }

    private List<Series> buildSeries(FaSettlementTb faSettlementTb, FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, Integer inputDecAmount) throws Throwable {
        //STEP1:获取选定日期的pay
        List<PayTb> payTbList = getPayTbsByDate(faSettlementTb.getDate() + "", franchiseeSiteTb);
        List<Series> originSeries = genOriginSeries(payTbList);

        List<Series> seriesList = null;
        DecData decData = calculateAmount(payTbList, inputDecAmount);

        if(CollectionUtils.isEmpty(payTbList)){
            return null;
        }
        if (decData.getSum()>320||payTbList.size()>11) {
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
                if (series.getPayTb().getAmount() * 3 > decData.getSum() && !decData.isInputDec()) {
                    k++;
                    continue;
                }
                if (set.contains(series.getPayTb().getId())) {
                    k++;
                    continue;
                }
                if (k % i == 0) {
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

    private List<Series> genOriginSeries(List<PayTb> payTbList) {
        return payTbList.stream().map(i -> new Series(i)).collect(Collectors.toList());
    }

    //根据选定history 的计算额度
    public DecData calculateAmount(List<PayTb> payTbList, Integer inputDecAmount) {
        DoubleSummaryStatistics stats = payTbList.stream()
                .collect(Collectors.summarizingDouble(PayTb::getAmount));
        double sum = stats.getSum();
        int decAmount = inputDecAmount != null ? inputDecAmount : (int) (sum / 9);//程序内限制的amount,需要同事满足两个
        return new DecData(sum, decAmount,inputDecAmount!=null);
    }

    private List<FranchiseeSiteTb> getFranchiseeSiteTbs(Integer inputVendorId) {
        QueryWrapper<FranchiseeSiteTb> franchiseeTbQueryWrapper = new QueryWrapper();
        franchiseeTbQueryWrapper.eq("own_id", inputVendorId);
        List<FranchiseeSiteTb> franchiseeSiteTbs = franchiseeSiteTbMapper.selectList(franchiseeTbQueryWrapper);
        return franchiseeSiteTbs;
    }

    private List<PayTb> getPayTbsByDate(String selectedDate, FranchiseeSiteTb franchiseeSiteTb) throws ParseException {
        QueryWrapper<PayTb> payTbQueryWrapper = new QueryWrapper();
        long dateTimeStart = SIMPLE_DATE_FORMAT.parse(selectedDate).getTime() / 1000;
        long dateTimeEnd = dateTimeStart + 24 * 60 * 60;
        payTbQueryWrapper.eq("status", 1)
                .ge("created_at", dateTimeStart)
                .le("created_at", dateTimeEnd)
                .eq("site_id", franchiseeSiteTb.getSiteId());

        List<PayTb> payTbList = payTbMapper.selectList(payTbQueryWrapper);
        payTbList.stream().forEach(i -> dateGenerator.generateDate(i));
        return payTbList;
    }

    private DailyData selectHistoryDate(FranchiseeSiteTb franchiseeSiteTb, double siteSum, Integer inputVendorId) {
        QueryWrapper<DailyPaperTb> dailyPaperTbQueryWrapper = new QueryWrapper();
        long lastDateTime=(System.currentTimeMillis() / 1000) - 25 * 24 * 60 * 60;
        int lastDate=Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(lastDateTime*1000)));

        dailyPaperTbQueryWrapper
                .eq("site_id", franchiseeSiteTb.getSiteId())
                .le("date",lastDate);
        List<DailyPaperTb> temp = dailyPaperTbMapper.selectList(dailyPaperTbQueryWrapper);
        if (temp == null || temp.size() < 5) {
            return null;
        }
        if (siteSum< 6800) {//低于这个就没必要了
            return null;
        }
        for (DailyPaperTb dailyPaperTb : temp) {
            if (!judgeExists(inputVendorId, franchiseeSiteTb.getSiteId(), dailyPaperTb.getDate())) {
                if (Math.abs(dailyPaperTb.getVendorRechargeAmount() - siteSum) < 3000) {
                    return new DailyData(dailyPaperTb,
                            getFaSettlementTb(inputVendorId,franchiseeSiteTb.getSiteId(),dailyPaperTb.getDate()));
                }
            }
        }

        DailyPaperTb res = null;
        double minDiff = 9999999;
        for (DailyPaperTb dailyPaperTb : temp) {
            if (!judgeExists(inputVendorId, franchiseeSiteTb.getSiteId(), dailyPaperTb.getDate())) {
                if (Math.abs(dailyPaperTb.getVendorRechargeAmount() - siteSum) < minDiff) {
                    minDiff = Math.abs(dailyPaperTb.getVendorRechargeAmount() - siteSum);
                    res = dailyPaperTb;
                }
            }
        }
        return new DailyData(res,getFaSettlementTb(inputVendorId,franchiseeSiteTb.getSiteId(),res.getDate()));
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

    //先计算当天的income
    private DoubleSummaryStatistics getTodayIncome(FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId) throws ParseException {
        QueryWrapper<VendorProfitSharingTb> vendorProfitSharingTbQueryWrapper = new QueryWrapper();

        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String sDate = "";
        if (hour < 6) {
            sDate = SIMPLE_DATE_FORMAT.format(new Date(time - 24 * 60 * 60 * 1000));//如果是凌晨需要取前一天的日期
        } else {
            sDate = SIMPLE_DATE_FORMAT.format(new Date(time));
        }
        long lastDayTime = SIMPLE_DATE_FORMAT.parse(sDate).getTime() / 1000;

        vendorProfitSharingTbQueryWrapper
                .eq("site_id", franchiseeSiteTb.getSiteId())
                .eq("type", 1)
                .eq("vendor_id", inputVendorId)
                .ge("created_at", lastDayTime)
                .le("created_at", lastDayTime + 24 * 60 * 60);

        //获取当天income
        List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(vendorProfitSharingTbQueryWrapper);
        DoubleSummaryStatistics todayVendorSum = vendorProfitSharingTbs.stream().collect(Collectors.summarizingDouble(VendorProfitSharingTb::getAmount));
        return todayVendorSum;
    }


    private List<Series> collectSeries(List<Series> originSeries, FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId, Integer inputDecAmount) throws Throwable {
        if (originSeries == null || originSeries.size() < 3) {//如果输入了金额 那就不管有几个单子了
            if (inputDecAmount == null) {
                return null;
            }
        }
        List<Series> seriesList = new ArrayList<>();
        for (Series series : originSeries) {
            try {
                PayTb payTb = series.getPayTb();
                QueryWrapper<CommodityOrdersTb> commodityOrderTbQueryWrapper = new QueryWrapper<>();
                commodityOrderTbQueryWrapper.eq("pay_sn", payTb.getPaySn()).eq("site_id", payTb.getSiteId());
                List<CommodityOrdersTb> commodityOrderTbs = commodityOrdersTbMapper.selectList(commodityOrderTbQueryWrapper);
                if (commodityOrderTbs == null || commodityOrderTbs.size() == 0) {
                    continue;
                }
                CommodityOrdersTb commodityOrderTb = commodityOrderTbs.get(0);
                series.setCommodityOrderTb(commodityOrderTb);
                dateGenerator.generateDate(commodityOrderTb);

                QueryWrapper<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbQueryWrapper = new QueryWrapper<>();
                commodityOrderProfitSharingTbQueryWrapper.eq("site_id", payTb.getSiteId())
                        .eq("order_id", commodityOrderTb.getOrderId());
                List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = commodityOrderProfitSharingTbMapper.selectList(commodityOrderProfitSharingTbQueryWrapper);
                if (commodityOrderProfitSharingTbs == null || commodityOrderProfitSharingTbs.size() == 0) {
                    continue;
                }
                commodityOrderProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
                series.setCommodityOrderProfitSharingTbs(commodityOrderProfitSharingTbs);
                DeliveryMethodType deliveryMethodType = DeliveryMethodType.from(commodityOrderProfitSharingTbs.get(0).getDeliveryMethod());
                QueryWrapper<VendorProfitSharingTb> vendorProfitSharingTbQueryWrapper = new QueryWrapper<>();
                vendorProfitSharingTbQueryWrapper.eq("site_id", payTb.getSiteId())
                        .eq("type", 1)
                        .eq("vendor_id", inputVendorId)
                        .in("transaction_id", commodityOrderProfitSharingTbs.stream().map(i -> i.getTransactionId()).collect(Collectors.toList()));
                List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(vendorProfitSharingTbQueryWrapper);
                if (vendorProfitSharingTbs == null || vendorProfitSharingTbs.size() == 0) {
                    continue;
                }
                vendorProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
                series.setVendorProfitSharingTbs(vendorProfitSharingTbs);
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
                    QueryWrapper<OrdersTb> ordersTbQueryWrapper = new QueryWrapper<>();
                    ordersTbQueryWrapper.eq("uid", commodityOrderTb.getUid())
                            .ge("created_at", commodityOrderTb.getCreatedAt())
                            .eq("site_id", payTb.getSiteId())
                            .eq(StringUtils.isNotEmpty(commodityOrderId), "commodity_order_id", commodityOrderId);
                    List<OrdersTb> ordersTbs = ordersTbMapper.selectList(ordersTbQueryWrapper);

                    final long exp=expireTime;
                    List<OrdersTb> lastOrders=new ArrayList<>();
                    if(CollectionUtils.isNotEmpty(ordersTbs)){
                        lastOrders=ordersTbs.stream().filter(i->i.getCreatedAt()>exp).collect(Collectors.toList());
                    }
                    if(CollectionUtils.isNotEmpty(lastOrders)) {
                        long lastMonthTime = System.currentTimeMillis() / 1000 - 30 * 24 * 60 * 60;
                        boolean exists = lastOrders.stream().anyMatch(i -> i.getCreatedAt() > lastMonthTime);
                        if(exists){
                            continue;//用户最近使用
                        }
                    }


                    List<OrdersTb> resOrdersTbs = new ArrayList<>();
                    //次卡是否都是一次？
                    if (ordersTbs != null && ordersTbs.size() > 0) {
                        if (deliveryMethodType == DeliveryMethodType.COUPON_WASHING) {
                            resOrdersTbs.add(ordersTbs.get(0));
                        } else if (deliveryMethodType == DeliveryMethodType.PREPAID) {
                            if (ordersTbs.size() >= commodityOrderProfitSharingTbs.size()) {
                                resOrdersTbs.addAll(ordersTbs.subList(0, commodityOrderProfitSharingTbs.size()));
                            } else {
                                resOrdersTbs.addAll(ordersTbs);
                            }
                        } else if (deliveryMethodType == DeliveryMethodType.VIP_TIME) {
                            List<OrdersTb> vipOrders=ordersTbs.stream().filter(i->i.getCreatedAt()<=exp).collect(Collectors.toList());
                            resOrdersTbs.addAll(vipOrders);
                        }
                    }
                    ordersTbs.stream().forEach(i -> dateGenerator.generateDate(i));
                    series.setOrdersTbs(resOrdersTbs);
                    seriesList.add(series);
                }
            } catch (Exception e) {
                LOGGER.error("{},e->{}", originSeries, ExceptionUtils.getStackTrace(e));
                throw new Throwable(ExceptionUtils.getStackTrace(e));
            }
        }
        return seriesList;

    }

}
