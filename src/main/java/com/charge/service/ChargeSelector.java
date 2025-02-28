package com.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.charge.entity.*;
import com.charge.mapper.*;
import com.wash.cache.DateCache;
import com.wash.entity.*;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.*;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.entity.statistics.FaSettlementTb;
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
    private ChargeOrderMapper chargeOrderMapper;

    @Autowired
    private SiteVendorMapper siteVendorMapper;

    @Autowired
    private StatementDailyMapper statementDailyMapper;
    @Autowired
    private StatementVendorDailyMapper statementVendorDailyMapper;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private VendorProfitSharingMapper vendorProfitSharingMapper;

    @Autowired
    private FaSettlementTbMapper faSettlementTbMapper;

    @Autowired
    private CommodityOrderMapper commodityOrderMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private CommodityOrderProfitSharingMapper commodityOrderProfitSharingMapper;
    @Autowired
    private SiteLatestDataMapper siteLatestDataTbTbMapper;

    @Autowired
    private DateCache dateCache;

    @Autowired
    private DateGenerator dateGenerator;

    private static Calendar calendar = Calendar.getInstance();
    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;
    @Autowired
    private CharRecorder charRecorder;

    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();


    public String select(Integer inputVendorId, Integer inputSiteId, Integer inputDate, Integer inputDecAmount) throws Exception {

        StringBuffer res=new StringBuffer();
        //STEP0 获取vendor 场地
        List<SiteVendor> siteVendors = getFranchiseeSiteTbs(inputVendorId);
        if (CollectionUtils.isEmpty(siteVendors))
            return "未获取到当前franchise";

        List<Integer> siteIds=siteVendors.stream().map(i->i.getSiteId()).collect(Collectors.toList());

        List<SiteLatestData> siteLatestData = getSiteLastDatas(siteIds);

        double vendorIncome = siteLatestData.stream().mapToDouble(SiteLatestData::getRechargeAmount).sum();

        Integer selectDate = selectDate(inputVendorId, siteIds, vendorIncome);

        if(selectDate==null){
            return res.toString();
        }

        long startTime=SIMPLE_DATE_FORMAT.parse(selectDate+"").getTime()/1000;
        QueryWrapper<Pay> payQueryWrapper=new QueryWrapper<>();
        payQueryWrapper.in("site_id",siteIds)
                .ge("created_at",startTime)
                .le("created_at",startTime+24*60*60);

        List<Pay>  pays=payMapper.selectList(payQueryWrapper);
        if(CollectionUtils.isEmpty(pays)){
            return res.toString();
        }
        List<CharEntity> charEntities=fillEntity(inputVendorId,pays);

        List<CharEntity> resEnetities=filterEntity(inputVendorId,vendorIncome,charEntities);


        CharModifier charModifier=new CharModifier(resEnetities);

        charModifier.calculate(inputVendorId);

        charRecorder.record(inputVendorId,selectDate,resEnetities,charModifier);


        //每个场地单独处理

        return res.toString();
    }

    private List<CharEntity> filterEntity(Integer inputVendorId, double vendorIncome, List<CharEntity> charEntities) {

        double calAmount = 0;
        double calSum = vendorIncome / 100;
        if (calSum < 100) {
            calAmount = vendorIncome / 5;
        } else if (calSum < 200) {
            calAmount = vendorIncome / 7;
        } else if (calSum < 400) {
            calAmount = vendorIncome / 8;
        } else if (calSum < 600) {
            calAmount = vendorIncome / 9;
        } else if (calSum < 800) {
            calAmount = vendorIncome / 10;
        } else if (calSum < 2000) {
            calAmount = vendorIncome / 13;
        } else {
            calAmount = vendorIncome / 14;
        }
        charEntities.sort((a,b)-> (int) (a.getPay().getCreatedAt()-b.getPay().getCreatedAt()));

        List<CharEntity> res=new ArrayList<>();
        int tempAmount=0;
        Set<Integer> set=new HashSet<>();
        for (int i = 4; i > 1; i--) {
            Iterator<CharEntity> iterator = charEntities.iterator();
            int k = 0;
            while (iterator.hasNext()) {
                CharEntity charEntity = iterator.next();
                if (charEntity.getPay().getAmount() > 2 * calAmount) {
                    k++;
                    continue;
                }

                if (set.contains(charEntity.getPay().getId())) {
                    k++;
                    continue;
                }
                if (k % i == 0) {
                    double diff = tempAmount + charEntity.getPay().getAmount() - calAmount;
                    if (diff > 1000) {
                        int maxDiff = calMaxDiff(calAmount);
                        if (diff > maxDiff) {
                            k++;
                            continue;
                        }
                    }
                    res.add(charEntity);
                    tempAmount += charEntity.getPay().getAmount();
                    set.add(charEntity.getPay().getId());
                    if (tempAmount > calAmount - 500) {
                        return res;
                    }
                }
                k++;
            }
        }
        return res;
    }

    private List<CharEntity> fillEntity(int inputVendorId,List<Pay> pays) {

        List<CharEntity> charEntities=new ArrayList<>();
        for(Pay pay:pays){
            if(pay.getStatus()!=1){
                continue;
            }
            CharEntity charEntity=new CharEntity();
            charEntity.setPay(pay);
            QueryWrapper<CommodityOrder> commodityOrderQueryWrapper=new QueryWrapper<>();
            commodityOrderQueryWrapper.eq("trade_no",pay.getTradeNo())
                    .eq("site_id",pay.getSiteId());
            List<CommodityOrder> list=commodityOrderMapper.selectList(commodityOrderQueryWrapper);
            CommodityOrder commodityOrder=list.get(0);

            if(commodityOrder.getStatus()!=2){
                continue;
            }

            if(commodityOrder.getPaymentAmount()!=commodityOrder.getProfitSharingAmount()||commodityOrder.getPaymentAmount()!=commodityOrder.getUsedAmount()){
                continue;
            }

            QueryWrapper<CommodityOrderProfitSharing> commodityOrderProfitSharingQueryWrapper=new QueryWrapper<>();
            commodityOrderProfitSharingQueryWrapper
                    .eq("site_id",pay.getSiteId())
                    .eq("order_id",commodityOrder.getOrderId());
            List<CommodityOrderProfitSharing> commodityOrderProfitSharings=commodityOrderProfitSharingMapper.selectList(commodityOrderProfitSharingQueryWrapper);

            List<String> trans=commodityOrderProfitSharings.stream().map(CommodityOrderProfitSharing::getTransactionId).collect(Collectors.toList());
            QueryWrapper<VendorProfitSharing> vendorProfitSharingQueryWrapper=new QueryWrapper<>();

            vendorProfitSharingQueryWrapper.eq("site_id",pay.getSiteId())
                    .eq("vendor_id",inputVendorId)
                    .in("transaction_id",trans);
            List<VendorProfitSharing> vendorProfitSharingList=vendorProfitSharingMapper.selectList(vendorProfitSharingQueryWrapper);
            charEntity.setCommodityOrder(commodityOrder);
            charEntity.setCommodityOrderProfitSharingList(commodityOrderProfitSharings);
            charEntity.setVendorProfitSharingList(vendorProfitSharingList);
            charEntities.add(charEntity);

            QueryWrapper<ChargeOrder> chargeOrderQueryWrapper=new QueryWrapper<>();
            chargeOrderQueryWrapper.eq("uid",pay.getUid())
                    .ge("created_at",pay.getCreatedAt()-60);
            List<ChargeOrder> chargeOrders=chargeOrderMapper.selectList(chargeOrderQueryWrapper);
            chargeOrders.sort((a,b)-> (int) (a.getCreatedAt()-b.getCreatedAt()));

            int temp=0;
            List<ChargeOrder> chargeOrderRes=new ArrayList<>();
            for(ChargeOrder chargeOrder:chargeOrders){
                temp+=chargeOrder.getPaymentBalance();
                chargeOrderRes.add(chargeOrder);
                if(temp>=pay.getAmount()){
                    break;
                }
            }
            charEntity.setChargeOrders(chargeOrderRes);
        }
        return charEntities;
    }

    private Integer  selectDate(Integer inputVendorId, List<Integer> siteIds, double vendorIncome) {

        QueryWrapper<StatementVendorDaily> statementDailyQueryWrapper = new QueryWrapper();
        long lastDateTime = (System.currentTimeMillis() / 1000) - 25 * 24 * 60 * 60;
        int lastDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(lastDateTime * 1000)));
        long firstTime = System.currentTimeMillis() / 1000 - 540 * 24 * 60 * 60;
        int firstDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(firstTime * 1000)));

        statementDailyQueryWrapper.eq("vendor_id",inputVendorId)
                .ge("date", firstDate)
                .le("date", lastDate);

        List<StatementVendorDaily> statementDailies=statementVendorDailyMapper.selectList(statementDailyQueryWrapper);
        TreeMap<Integer, List<StatementVendorDaily>> dateMap = statementDailies.stream()
                .collect(Collectors.toMap(
                        StatementVendorDaily::getDate,
                        statementDaily -> {
                            List<StatementVendorDaily> list = new ArrayList<>();
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
            if(dateCache.C_DATE_MAP.containsKey(inputVendorId)){
                if(dateCache.C_DATE_MAP.get(inputVendorId).contains(date)){
                    continue;
                }
            }
            double chargeSum=dateMap.get(date).stream().mapToDouble(i->i.getProfitSharingTotalAmount()).sum();
            if(Math.abs(chargeSum-vendorIncome)<3000){
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



    //计算最大冗余
    private static int calMaxDiff(double decData) {
        int maxDiff = 0;
        if (decData / 10000 == 0) {
            maxDiff = 2000;
        } else if (decData/ 10000 == 1) {
            maxDiff = 3500;
        } else if (decData/ 10000 == 2) {
            maxDiff = 5200;
        } else {
            maxDiff = 7000;
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



    public boolean judgeExists(int inputVendorId, int siteId, int date) {
        Set<Integer> dateSet = new HashSet<>();
        if (dateCache.SITE_DATE_MAP.containsKey(inputVendorId)) {
            if (dateCache.SITE_DATE_MAP.get(inputVendorId).containsKey(siteId)) {
                dateSet = dateCache.SITE_DATE_MAP.get(inputVendorId).get(siteId);
            }
        }
        return dateSet.contains(date);
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

}
