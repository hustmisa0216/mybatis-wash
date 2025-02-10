package com.wash.select;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wash.cache.DateCache;
import com.wash.entity.DecData;
import com.wash.entity.Series;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.data.*;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.mapper.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Select {
    private static final Logger LOGGER = LoggerFactory.getLogger(Select.class);

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private String vendorId = "3230";
    //private String siteId = "865";
    private int inputDecAmount = 500;
    @Autowired
    private FranchiseeSiteTbMapper franchiseeSiteTbMapper;

    @Autowired
    private PayTbMapper payTbMapper;
    @Autowired
    private VendorProfitSharingTbMapper vendorProfitSharingTbMapper;

    @Autowired
    private FaSettlementTbMapper faSettlementTbMapper;

    @Autowired
    private CommodityOrderTbsMapper commodityOrderTbMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private CommodityOrderProfitSharingTbMapper commodityOrderProfitSharingTbMapper;

    @Autowired
    private DateCache dateCache;

    private static Calendar calendar = Calendar.getInstance();

    @PostConstruct
    public void select() {
        try {
            //STEP0 获取vendor 场地
            List<FranchiseeSiteTb> franchiseeSiteTbs = getFranchiseeSiteTbs();

            //每个场地单独处理
            for (FranchiseeSiteTb franchiseeSiteTb : franchiseeSiteTbs) {
                DoubleSummaryStatistics todayVendorSum = getTodayIncome(franchiseeSiteTb);
                FaSettlementTb faSettlementTbRes = selectHistoryDate(franchiseeSiteTb, todayVendorSum);
                if (faSettlementTbRes==null) continue;

                List<Series> resSeries = buildSeries(faSettlementTbRes.getDate()+"", franchiseeSiteTb);

                System.out.println(resSeries);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<Series> buildSeries(String selectedDate, FranchiseeSiteTb franchiseeSiteTb) throws ParseException {
        //STEP1:获取选定日期的pay
        List<PayTb> payTbList = getPayTbsByDate(selectedDate, franchiseeSiteTb);
        List<Series> seriesList = collectSeries(payTbList, franchiseeSiteTb);
        DecData decData = calculateAmount(payTbList);

        int tempAmount = 0;
        List<Series> resSeries = new ArrayList<>();
        Iterator<Series> iterator = seriesList.iterator();
        for (int i = 4; i > 1; i--) {
            int k = 0;
            while (iterator.hasNext()) {
                Series series = iterator.next();
                if (series.getPayTb().getAmount() * 3 > decData.getSum()) {
                    iterator.remove();
                }
                if (k % i == 0) {
                    resSeries.add(series);
                    tempAmount += series.getPayTb().getAmount();
                    iterator.remove();
                    if (tempAmount > decData.getDecAmount() || tempAmount > inputDecAmount * 100) {
                        break;
                    }
                }
                k++;
            }
            if (tempAmount > decData.getDecAmount() || tempAmount > inputDecAmount * 100) {
                break;
            }
        }
        return resSeries;
    }

    //根据选定history 的计算额度
    public DecData calculateAmount(List<PayTb> payTbList) {
        DoubleSummaryStatistics stats = payTbList.stream()
                .collect(Collectors.summarizingDouble(PayTb::getAmount));
        double sum = stats.getSum();
        int decAmount = (int) (sum / 8);//程序内限制的amount,需要同事满足两个
        return new DecData(sum, decAmount);
    }

    private List<FranchiseeSiteTb> getFranchiseeSiteTbs() {
        QueryWrapper<FranchiseeSiteTb> franchiseeTbQueryWrapper = new QueryWrapper();
        franchiseeTbQueryWrapper.eq("own_id", vendorId);
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
        return payTbList;
    }

    private FaSettlementTb selectHistoryDate(FranchiseeSiteTb franchiseeSiteTb, DoubleSummaryStatistics todayVendorSum) {
        QueryWrapper<FaSettlementTb> faSettlementTbQueryWrapper = new QueryWrapper();
        faSettlementTbQueryWrapper
                .eq("site_id", franchiseeSiteTb.getSiteId())
                .eq("own_id", vendorId);
        List<FaSettlementTb> temp = faSettlementTbMapper.selectList(faSettlementTbQueryWrapper);
        if(temp==null||temp.size()<5){
            return null;
        }
        List<FaSettlementTb> faSettlementTbs=temp.stream().filter(i->System.currentTimeMillis()/1000-i.getCreatedAt()>=25*24*60*60).collect(Collectors.toList());

        if (todayVendorSum.getSum() < 7800) {//低于这个就没必要了
            return null;
        }
        //过滤处理过的日期
        Set<String> dateSet = new HashSet<>();
        if (dateCache.SITE_DATE_MAP.containsKey(vendorId)) {
            if (dateCache.SITE_DATE_MAP.get(vendorId).containsKey(franchiseeSiteTb.getSiteId())) {
                dateSet = dateCache.SITE_DATE_MAP.get(vendorId).get(franchiseeSiteTb.getSiteId());
            }
        }


        for (FaSettlementTb faSettlementTb : faSettlementTbs) {
            if (!dateSet.contains(faSettlementTb.getDate())) {
                if (Math.abs(faSettlementTb.getEarnings() - todayVendorSum.getSum()) < 3000) {
                    return faSettlementTb;
                }
            }
        }

        FaSettlementTb res=null;
        double minDiff = 9999999;
        for (FaSettlementTb faSettlementTb : faSettlementTbs) {
            if (!dateSet.contains(faSettlementTb.getDate())) {
               if(Math.abs(faSettlementTb.getEarnings() - todayVendorSum.getSum())<minDiff){
                   minDiff=Math.abs(faSettlementTb.getEarnings() - todayVendorSum.getSum());
                   res=faSettlementTb;
                }
            }

        }

        return res;
    }

    //先计算当天的income
    private DoubleSummaryStatistics getTodayIncome(FranchiseeSiteTb franchiseeSiteTb) throws ParseException {
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
                .eq("vendor_id", vendorId)
                .ge("created_at", lastDayTime)
                .le("created_at", lastDayTime + 24 * 60 * 60);

        //获取当天income
        List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(vendorProfitSharingTbQueryWrapper);
        DoubleSummaryStatistics todayVendorSum = vendorProfitSharingTbs.stream().collect(Collectors.summarizingDouble(VendorProfitSharingTb::getAmount));
        return todayVendorSum;
    }


    private List<Series> collectSeries(List<PayTb> payTbList, FranchiseeSiteTb franchiseeSiteTb) {
        if (payTbList == null || payTbList.size() < 3) {
            return null;
        }
        try {
            List<Series> seriesList = new ArrayList<>();
            for (PayTb payTb : payTbList) {
                Series series = new Series(payTb);
                QueryWrapper<CommodityOrdersTb> commodityOrderTbQueryWrapper = new QueryWrapper<>();
                commodityOrderTbQueryWrapper.eq("pay_sn", payTb.getPaySn()).eq("site_id", payTb.getSiteId());
                List<CommodityOrdersTb> commodityOrderTbs = commodityOrderTbMapper.selectList(commodityOrderTbQueryWrapper);
                if (commodityOrderTbs == null || commodityOrderTbs.size() == 0) {
                    continue;
                }
                CommodityOrdersTb commodityOrderTb = commodityOrderTbs.get(0);
                series.setCommodityOrderTb(commodityOrderTb);

                QueryWrapper<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbQueryWrapper = new QueryWrapper<>();
                commodityOrderProfitSharingTbQueryWrapper.eq("site_id", payTb.getSiteId())
                        .eq("order_id", commodityOrderTb.getOrderId());
                List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = commodityOrderProfitSharingTbMapper.selectList(commodityOrderProfitSharingTbQueryWrapper);
                if (commodityOrderProfitSharingTbs == null || commodityOrderProfitSharingTbs.size() == 0) {
                    continue;
                }
                series.setCommodityOrderProfitSharingTbs(commodityOrderProfitSharingTbs);
                DeliveryMethodType deliveryMethodType = DeliveryMethodType.from(commodityOrderProfitSharingTbs.get(0).getDeliveryMethod());
                QueryWrapper<VendorProfitSharingTb> vendorProfitSharingTbQueryWrapper = new QueryWrapper<>();
                vendorProfitSharingTbQueryWrapper.eq("site_id", payTb.getSiteId())
                        .eq("type", 1)
                        .eq("vendor_id", vendorId)
                        .in("transaction_id", commodityOrderProfitSharingTbs.stream().map(i -> i.getTransactionId()).collect(Collectors.toList()));
                List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(vendorProfitSharingTbQueryWrapper);
                if (vendorProfitSharingTbs == null || vendorProfitSharingTbs.size() == 0) {
                    continue;
                }
                series.setVendorProfitSharingTbs(vendorProfitSharingTbs);
                //结算额度
                DoubleSummaryStatistics orderProfit = commodityOrderProfitSharingTbs.stream().collect(Collectors.summarizingDouble(CommodityOrderProfitSharingTb::getRechargeAmount));
                Long expireTime = commodityOrderTb.getVipExpiredAt() == null ? commodityOrderTb.getPrepaidExpiredAt() : commodityOrderTb.getVipExpiredAt();
                if (expireTime == null) {
                    expireTime = commodityOrderTb.getCreatedAt() + 24 * 60 * 60 * 30;
                }

                //结算完毕
                if (orderProfit.getSum() == payTb.getAmount() || expireTime <= System.currentTimeMillis() / 1000) {
                    QueryWrapper<OrdersTb> ordersTbQueryWrapper = new QueryWrapper<>();
                    ordersTbQueryWrapper.eq("uid", commodityOrderTb.getUid())
                            .eq("pay_sn", payTb.getPaySn())
                            .ge("created_at", commodityOrderTb.getCreatedAt())
                            .le("created_at", expireTime)
                            .eq("site_id", payTb.getSiteId());
                    List<OrdersTb> ordersTbs = ordersTbMapper.selectList(ordersTbQueryWrapper);

                    List<OrdersTb> resOrdersTbs = new ArrayList<>();
                    //次卡是否都是一次？
                    if (ordersTbs != null && ordersTbs.size() > 0) {
                        if (deliveryMethodType == DeliveryMethodType.PER_USE_CARD) {
                            resOrdersTbs.add(ordersTbs.get(0));
                        } else if (deliveryMethodType == DeliveryMethodType.PREPAID) {
                            if (ordersTbs.size() >= commodityOrderProfitSharingTbs.size()) {
                                resOrdersTbs.addAll(ordersTbs.subList(0, commodityOrderProfitSharingTbs.size()));
                            } else {
                                resOrdersTbs.addAll(ordersTbs);
                            }
                        } else if (deliveryMethodType == DeliveryMethodType.VIP_TIME) {
                            resOrdersTbs.addAll(ordersTbs);
                        }
                    }
                    series.setOrdersTbs(ordersTbs);
                    seriesList.add(series);
                }

            }

            return seriesList;
        } catch (Exception e) {
            LOGGER.error("{},e->{}", payTbList, ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

}
