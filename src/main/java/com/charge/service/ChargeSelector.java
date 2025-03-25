package com.charge.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.charge.entity.*;
import com.charge.mapper.*;
import com.wash.cache.DateCache;
import com.wash.mapper.*;
import com.wash.service.Modifier;
import com.wash.service.Recorder;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@DS("char")
public class ChargeSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeSelector.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private Recorder recorder;

    @Autowired
    private UserMapper userMapper;

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
    private Handler handler;

    @Autowired
    private DateGenerator dateGenerator;

    private static Calendar calendar = Calendar.getInstance();
    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;
    @Autowired
    private CharRecorder charRecorder;

    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    @Autowired
    private VendorMapper vendorMapper;

    public String select(Integer inputVendorId, Integer inputAmount) throws Exception {

        StringBuffer res=new StringBuffer();
        //STEP0 获取vendor 场地
        List<SiteVendor> siteVendors = getFranchiseeSiteTbs(inputVendorId);
        if (CollectionUtils.isEmpty(siteVendors))
            return "未获取到当前franchise";

        List<Integer> siteIds=siteVendors.stream().map(i->i.getSiteId()).collect(Collectors.toList());

        List<SiteLatestData> siteLatestData = getSiteLastDatas(siteIds);

        double queryIncome=0;

        if(inputAmount!=null){
            queryIncome=inputAmount*900;
        }else {
            queryIncome = siteLatestData.stream().mapToDouble(SiteLatestData::getRechargeAmount).sum();
        }
        SelectInfo selectInfo = selectDate(inputVendorId, siteIds, queryIncome);

        if(selectInfo==null){
            return res.toString();
        }

        long startTime=SIMPLE_DATE_FORMAT.parse(selectInfo.getDate()+"").getTime()/1000;
        QueryWrapper<Pay> payQueryWrapper=new QueryWrapper<>();
        payQueryWrapper.in("site_id",siteIds)
                .ge("created_at",startTime)
                .le("created_at",startTime+24*60*60);

        List<Pay>  pays=payMapper.selectList(payQueryWrapper);
        if(CollectionUtils.isEmpty(pays)){
            return res.toString();
        }
        List<CharEntity> charEntities=fillEntity(inputVendorId,pays);

        List<CharEntity> resEnetities=filterEntity(inputVendorId,selectInfo,charEntities,inputAmount);

        CharModifier charModifier=new CharModifier(selectInfo.getDate(),pays,resEnetities);

        charModifier.calculate(inputVendorId);

        handler.de(inputVendorId,charModifier);
        Vendor b=vendorMapper.selectById(inputVendorId);
        charModifier.setBefore(b.getUndrawnAmount());
        handler.update(inputVendorId,charModifier);
        Vendor after=vendorMapper.selectById(inputVendorId);
        charModifier.setAfter(after.getUndrawnAmount());
        charModifier.buildKey();

        charRecorder.record(inputVendorId,selectInfo.getDate(),resEnetities,charModifier);
        //每个场地单独处理

        return charModifier.buildKey();
    }

    private List<CharEntity> filterEntity(Integer inputVendorId, SelectInfo selectInfo, List<CharEntity> charEntities, Integer inputAmount) {

        double vendorIncome=selectInfo.getAmount();
        double calAmount = 0;
        double calSum = vendorIncome / 100;
        if (calSum < 100) {
            calAmount = vendorIncome / 4;
        } else if (calSum < 200) {
            calAmount = vendorIncome / 5;
        } else if (calSum < 400) {
            calAmount = vendorIncome / 6;
        } else if (calSum < 600) {
            calAmount = vendorIncome / 7;
        } else if (calSum < 800) {
            calAmount = vendorIncome / 8;
        } else if (calSum < 2000) {
            calAmount = vendorIncome / 11;
        } else {
            calAmount = vendorIncome / 12;
        }

        if(inputAmount!=null){
            calAmount=inputAmount*100;
        }
        charEntities.sort((a,b)-> (int) (a.getPay().getCreatedAt()-b.getPay().getCreatedAt()));

        List<CharEntity> res=new ArrayList<>();
        int tempAmount=0;
        Set<Integer> set=new HashSet<>();
        for (int i = 5; i > 1; i--) {
            Iterator<CharEntity> iterator = charEntities.iterator();
            int k = 0;
            while (iterator.hasNext()) {
                CharEntity charEntity = iterator.next();
                if (charEntity.getPay().getAmount() > 2 * calAmount||charEntity.getPay().getAmount() >7000) {
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

            User user=userMapper.selectById(commodityOrder.getUid());
            double balance=user.getBalanceRecharge();
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
            if(CollectionUtils.isEmpty(vendorProfitSharingList)){
                continue;
            }
            charEntity.setCommodityOrder(commodityOrder);
            charEntity.setCommodityOrderProfitSharingList(commodityOrderProfitSharings);
            charEntity.setVendorProfitSharingList(vendorProfitSharingList);


            QueryWrapper<ChargeOrder> chargeOrderQueryWrapper=new QueryWrapper<>();
            chargeOrderQueryWrapper.eq("uid",pay.getUid())
                    .ge("created_at",pay.getCreatedAt()-60);
            List<ChargeOrder> chargeOrders=chargeOrderMapper.selectList(chargeOrderQueryWrapper);

            chargeOrders.sort((a,b)-> (int) (a.getCreatedAt()-b.getCreatedAt()));

            if(chargeOrders.size()<30&&chargeOrders.get(chargeOrders.size()-1).getCreatedAt()>System.currentTimeMillis()/1000-26*24*60*60){
                continue;
            }
            int temp=0;
            List<ChargeOrder> chargeOrderRes=new ArrayList<>();
            for(ChargeOrder chargeOrder:chargeOrders){
                temp+=chargeOrder.getPaymentBalance();
                chargeOrderRes.add(chargeOrder);
                if(temp>=pay.getAmount()){
                    break;
                }
            }
            if(balance>1000&&temp>pay.getAmount()+200){
                continue;
            }
            charEntity.setChargeOrders(chargeOrderRes);
            charEntities.add(charEntity);
        }
        return charEntities;
    }

    private SelectInfo  selectDate(Integer inputVendorId, List<Integer> siteIds, double vendorIncome) {

        QueryWrapper<StatementsVendorDaily> statementDailyQueryWrapper = new QueryWrapper();
        long lastDateTime = (System.currentTimeMillis() / 1000) - 27 * 24 * 60 * 60;
        int lastDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(lastDateTime * 1000)));
        long firstTime = System.currentTimeMillis() / 1000 - 430 * 24 * 60 * 60;
        int firstDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(firstTime * 1000)));

        statementDailyQueryWrapper.eq("vendor_id",inputVendorId)
                .ge("date", firstDate)
                .le("date", lastDate);

        List<StatementsVendorDaily> statementDailies=statementVendorDailyMapper.selectList(statementDailyQueryWrapper);
        TreeMap<Integer, List<StatementsVendorDaily>> dateMap = statementDailies.stream()
                .collect(Collectors.toMap(
                        StatementsVendorDaily::getDate,
                        statementDaily -> {
                            List<StatementsVendorDaily> list = new ArrayList<>();
                            list.add(statementDaily);
                            return list;
                        },
                        (existing, replacement) -> {
                            existing.addAll(replacement);
                            return existing;
                        },
                        TreeMap::new // 使用 TreeMap
                ));

        TreeMap<Integer,Double> dateSumMap=dateMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().mapToDouble(StatementsVendorDaily::getProfitSharingIncomeAmount).sum(),
                (existing, replacement) -> existing, // 保留第一个值
                TreeMap::new // 使用 TreeMap
        ));
        for(int date:dateSumMap.keySet()){
            if(dateCache.C_DATE_MAP.containsKey(inputVendorId)){
                if(dateCache.C_DATE_MAP.get(inputVendorId).contains(date)){
                    continue;
                }
            }
            double chargeSum=dateSumMap.get(date);
            if(Math.abs(chargeSum-vendorIncome)<8000||(chargeSum-vendorIncome<12000&&chargeSum-vendorIncome>0)){
                return new SelectInfo(date,chargeSum);
            }
        }

        double minDiff = Double.MAX_VALUE;

        SelectInfo selectInfo = null;
        for (Map.Entry<Integer, Double> entry : dateSumMap.entrySet()) {
            if (dateCache.C_DATE_MAP.containsKey(inputVendorId) &&
                    dateCache.C_DATE_MAP.get(inputVendorId).contains(entry.getKey())) {
                continue;
            }

            double diff = Math.abs(entry.getValue() - vendorIncome);
            if (diff < minDiff) {
                minDiff = diff;
                selectInfo=new SelectInfo(entry.getKey(),entry.getValue());
            }
        }
        return selectInfo;
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

    //计算最大冗余
    private static int calMaxDiff(double decData) {
        int maxDiff = 0;
        if (decData / 10000 == 0) {
            maxDiff = 3000;
        } else if (decData/ 10000 == 1) {
            maxDiff = 4500;
        } else if (decData/ 10000 == 2) {
            maxDiff = 6200;
        } else {
            maxDiff = 7000;
        }
        return maxDiff;
    }

    private List<SiteVendor> getFranchiseeSiteTbs(Integer inputVendorId) {
        QueryWrapper<SiteVendor> franchiseeTbQueryWrapper = new QueryWrapper();
        franchiseeTbQueryWrapper.eq("vendor_id", inputVendorId);
        List<SiteVendor> franchiseeSiteTbs = siteVendorMapper.selectList(franchiseeTbQueryWrapper);
        return franchiseeSiteTbs;
    }


}
