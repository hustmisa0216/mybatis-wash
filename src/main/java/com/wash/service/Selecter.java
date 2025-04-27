package com.wash.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.assist.ISqlRunner;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.cache.DateCache;
import com.wash.entity.u.UserTb;
import com.wash.service.calculator.VenCalculator;
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
import com.wash.service.calculator.DrawFilter;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.list.PredicatedList;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.wash.service.Recorder.*;

@Component
@DS("wash")
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
    private FaSettlementTbMapper faSettlementTbMapper;

    @Autowired
    private SiteLatestDataTbTbMapper siteLatestDataTbTbMapper;

    @Autowired
    private DateCache dateCache;

    private static Calendar calendar = Calendar.getInstance();
    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;
    @Autowired
    private Collector collector;

    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    @Autowired
    private DrawFilter drawCalculator;

    public String select(TaskRecord taskRecord, Integer inputSiteId, Integer inputDate, Integer inputDecAmount) throws Exception {

        int inputVendorId = taskRecord.getVen();
        //STEP0 获取ven 车辆行驶路线
        List<FranchiseeSiteTb> franchiseeSiteTbs = getFranchiseeSiteTbs(inputVendorId);
        if (CollectionUtils.isEmpty(franchiseeSiteTbs)) return "未获取到当前franchise";
        StringBuffer res = new StringBuffer();
        FranchiseeTb franchiseeTb = franchiseeTbMapper.selectById(inputVendorId);

        if(franchiseeTb.getWaitWithdraw()<1390*100){
            LOGGER.info("{},太低导致无线路可用:{}",inputVendorId,franchiseeTb.getWaitWithdraw()/100);
            return "无路线可用";
        }
        //每个场地单独处理
        CountDownLatch countDownLatch = new CountDownLatch(franchiseeSiteTbs.size());
        AtomicInteger totalIncome=new AtomicInteger();
        AtomicInteger paretnIncome=new AtomicInteger();
        for (FranchiseeSiteTb franchiseeSiteTb : franchiseeSiteTbs) {
            threadPoolExecutor.execute(() -> {
                try {
                    handleByFsite(franchiseeSiteTbs.size(), inputVendorId, inputSiteId, inputDate, inputDecAmount,
                            res, franchiseeTb, franchiseeSiteTb, countDownLatch,totalIncome,paretnIncome,taskRecord);
                } catch (Throwable e) {
                    countDownLatch.countDown();
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                }
            });
        }
        countDownLatch.await(30, TimeUnit.SECONDS);
        res.append("\n");
        res.append("总计:"+totalIncome.get()+"-"+paretnIncome.get());
        if(totalIncome.get()+paretnIncome.get()>0){
            taskRecord.getSiteMap().put(inputSiteId,new AtomicInteger(totalIncome.get()+paretnIncome.get()));
        }
        dateCache.reload();
        return res.toString();
    }



    private void handleByFsite(int size, Integer inputVendorId, Integer inputSiteId, Integer inputDate, Integer inputDecAmount, StringBuffer res, FranchiseeTb franchiseeTb, FranchiseeSiteTb franchiseeSiteTb,
                               CountDownLatch countDownLatch, AtomicInteger totalIncome, AtomicInteger paretnIncome, TaskRecord taskRecord) throws Throwable {
        if (franchiseeSiteTb.getDeletedAt() != null) {
            countDownLatch.countDown();
            return;
        }

        if (inputSiteId != null) {
            if (franchiseeSiteTb.getSiteId().intValue() != inputSiteId.intValue()) {
                countDownLatch.countDown();
                return;
            }
        }

        TodayData todayData = null;
        List<DailyData> dailyDatas = new ArrayList<>();

        if (inputDate == null) {
            todayData = getTodayIncome(franchiseeSiteTb, inputVendorId);
            if (todayData == null) {
                countDownLatch.countDown();
                return;
            }
            double lastDayEar = todayData.getLastDayEar();
            if (size > 2) {
                if (todayData.getSiteLatestDataTb().getRechargeAmount() > 13800 || lastDayEar > 9600||lastDayEar<0) {
                    int lastDayRecharge=todayData.getSiteLatestDataTb().getRechargeAmount();
                    int decMerge=0;
                    if(lastDayEar<100){
                        decMerge= (int) ((lastDayRecharge + 5*lastDayEar) / 6);
                    }else if(lastDayEar>=100&&lastDayEar<200){
                        decMerge= (int) ((lastDayRecharge + 4*lastDayEar) / 5);
                    }else if(lastDayEar>=200&&lastDayEar<400){
                        decMerge= (int) ((lastDayRecharge + 3*lastDayEar) / 4);
                    }else {
                        decMerge= (int) ((lastDayRecharge + 2*lastDayEar) / 3);
                    }
                    int calAmount= lastDayRecharge<lastDayEar?lastDayRecharge:decMerge;
                    double amount = inputDecAmount == null ? calAmount : inputDecAmount.intValue() * 3;
                    dailyDatas = selectHistoryDate(franchiseeSiteTb, amount, inputVendorId, inputDecAmount);
                }

            } else {
                if (inputVendorId.intValue()==11||(todayData.getSiteLatestDataTb().getRechargeAmount() > 11400 || lastDayEar > 8200)) {
                    int lastDayRecharge=todayData.getSiteLatestDataTb().getRechargeAmount();
                    int calAmount= lastDayRecharge<lastDayEar?lastDayRecharge: (int) ((lastDayRecharge + lastDayEar) / 2);
                    double amount = inputDecAmount == null ? calAmount : inputDecAmount.intValue() * 3;
                    dailyDatas = selectHistoryDate(franchiseeSiteTb, amount, inputVendorId, inputDecAmount);
                }
            }
        } else {
            if (judgeExists(inputVendorId, inputSiteId, inputDate)) {
                res.append(franchiseeSiteTb.getSiteId() + "-" + inputDate + "-" + "该日期已经处理,请谨慎输入\n");
                countDownLatch.countDown();
                return;
            } else {
                FaSettlementTb faSettlementTbRes = getFaSettlementTb(inputVendorId, franchiseeSiteTb.getSiteId(), inputDate);
                DailyPaperTb dailyPaperTb = getDailyDataTb(inputVendorId, franchiseeSiteTb.getSiteId(), inputDate);
                DailyData  dailyData = new DailyData(dailyPaperTb, faSettlementTbRes);
                dailyDatas.add(dailyData);
            }
        }

        if (CollectionUtils.isEmpty(dailyDatas)) {
            LOGGER.info("{},{},{}", inputVendorId, franchiseeSiteTb.getSiteId(), "未找到合适日期");
            res.append(inputVendorId + "-" + franchiseeSiteTb.getSiteId() + "-" + "未找到合适日期\n");
            countDownLatch.countDown();
            return;
        }
        taskRecord.getCurRe().addAndGet(todayData.getSiteLatestDataTb().getRechargeAmount()/100);
        taskRecord.getCurIn().addAndGet(todayData.getLastDayEar()/100);

        DailyData curDailyData =null;
        List<Series> resSeries=new ArrayList<>();
        for(DailyData dailyData:dailyDatas){
            if(dailyData!=null&&dailyData.getFaSettlementTb()!=null) {
                resSeries = collector.buildSeries(size, dailyData.getFaSettlementTb(), franchiseeSiteTb, inputVendorId, inputDecAmount);
                if (CollectionUtils.isNotEmpty(resSeries)) {
                    curDailyData = dailyData;
                    break;
                }
            }
        }

        if (CollectionUtils.isEmpty(resSeries)) {
            res.append(inputVendorId + "-" + franchiseeSiteTb.getSiteId() + "-" + JSON.toJSONString(dailyDatas) + "-未获取到任何条目\n");
            countDownLatch.countDown();
            return;
        }

        if (judgeOandPLess(inputVendorId, res, franchiseeTb, franchiseeSiteTb,resSeries)){
            countDownLatch.countDown();
            return;
        }

        ModifierData modifierData = updateAndDel(inputVendorId, franchiseeSiteTb, curDailyData, resSeries, franchiseeTb);
        updateFranchisee(inputVendorId, franchiseeSiteTb, modifierData);
        record(inputVendorId, franchiseeSiteTb, modifierData, curDailyData);
        totalIncome.addAndGet((int) Math.ceil(modifierData.getTotalIncome()/100));
        paretnIncome.addAndGet((int) Math.ceil(modifierData.getParentTotalIncome()/100));
        res.append(modifierData.getKey() + "||" + (int) Math.ceil(modifierData.getWaitWithDraw() / 100) + "-" + (int) Math.ceil(modifierData.getAfterWaitDraw() / 100) + "\n");
        countDownLatch.countDown();
    }

    private boolean judgeOandPLess(Integer inputVendorId, StringBuffer res, FranchiseeTb franchiseeTb, FranchiseeSiteTb franchiseeSiteTb, List<Series> resSeries) {
        boolean major= franchiseeSiteTb.getOwnPercent().doubleValue()> franchiseeSiteTb.getParentPercent().doubleValue();
        DrawFilter.LessReason lessReason =drawCalculator.drawCalculate(inputVendorId, franchiseeTb,major);
        if(!lessReason.isValid()){
            LOGGER.info("{},{},lessReason:{}", inputVendorId,franchiseeSiteTb.getSiteId(),lessReason.getReason());
            res.append(franchiseeTb.getId() +  "-"+lessReason.getReason()+"-" + "rest lesssssss\n");
            return true;
        }


        //这个应该是融合当时的车，而不是当前保存的(中途变化过)
        int parentId =resSeries.get(0).getParentVen();
        if(parentId!=-1){
            boolean allSame=resSeries.stream().allMatch(i->i.getParentVen()==parentId);
            if(!allSame){
                LOGGER.info("{},{},lessReasonNotSame:{},{}", inputVendorId,franchiseeSiteTb.getSiteId(),parentId,resSeries);
                return true;
            }
            FranchiseeTb parentFranchiseeSiteTb = franchiseeTbMapper.selectById(parentId);
            DrawFilter.LessReason parentLess = drawCalculator.drawCalculate(parentId, parentFranchiseeSiteTb, !major);
            if (!parentLess.isValid()) {
                LOGGER.info("{},{},lessReasonBecParent:{}:{}", inputVendorId,franchiseeSiteTb.getSiteId(), parentId, lessReason.getReason());
                res.append(parentFranchiseeSiteTb.getId() + "-" + parentLess.getReason() + "-" + "parentRest lesssssss\n");
                return true;
            }
        }
        return false;
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
            franchiseeTbUpdateWrapperParent.eq("id", modifierData.getParentId())
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

        FileWriter allWriter = new FileWriter(buildAllPath(inputVendorId) + FilesEnum.ALL.getFileName(), true);
        String today=SIMPLE_DATE_FORMAT.format(new Date());
        String all=franchiseeSiteTb.getSiteId()+","+today+","+dailyData.getFaSettlementTb().getDate()+","+
                (int) Math.ceil(modifierData.getWaitWithDraw() / 100)+","+(int) Math.ceil(modifierData.getAfterWaitDraw() / 100)+","+modifierData.getSimpleKey();
        allWriter.write(all+"\n");
        allWriter.flush();
    }

    @Transactional(rollbackFor = Exception.class)
        // 所有异常均触发回滚
    ModifierData updateAndDel(Integer inputVendorId, FranchiseeSiteTb franchiseeSiteTb, DailyData dailyData,
                              List<Series> resSeries, FranchiseeTb franchiseeTb) throws Exception {
        modifier.delete(resSeries);
        ModifierData modifierData = modifier.update(franchiseeTb, inputVendorId, dailyData, franchiseeSiteTb, resSeries);
        return modifierData;
    }

    private DailyPaperTb getDailyDataTb(Integer inputVendorId, Integer siteId, Integer inputDate) {
        QueryWrapper<DailyPaperTb> dailyPaperTbQueryWrapper = new QueryWrapper();
        dailyPaperTbQueryWrapper
                .eq("site_id", siteId)
                .le("date", inputDate);
        return dailyPaperTbMapper.selectList(dailyPaperTbQueryWrapper).get(0);
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

    private List<FranchiseeSiteTb> getFranchiseeSiteTbs(Integer inputVendorId) {
        QueryWrapper<FranchiseeSiteTb> franchiseeTbQueryWrapper = new QueryWrapper();
        franchiseeTbQueryWrapper.eq("own_id", inputVendorId);
        List<FranchiseeSiteTb> franchiseeSiteTbs = franchiseeSiteTbMapper.selectList(franchiseeTbQueryWrapper);
        return franchiseeSiteTbs;
    }


    private List<DailyData> selectHistoryDate(FranchiseeSiteTb franchiseeSiteTb, double siteSum, Integer inputVendorId, Integer inputDecAmount) throws ParseException {
        QueryWrapper<DailyPaperTb> dailyPaperTbQueryWrapper = new QueryWrapper();
        List<DailyData> dailyDatas=new ArrayList<>();
        long lastDateTime = (System.currentTimeMillis() / 1000) - 25 * 24 * 60 * 60;
        int lastDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(lastDateTime * 1000)));
        int dayBefore=inputVendorId.intValue()==3191?320:540;
        long firstTime = System.currentTimeMillis() / 1000 - dayBefore * 24 * 60 * 60;
        int firstDate = Integer.valueOf(SIMPLE_DATE_FORMAT.format(new Date(firstTime * 1000)));

        dailyPaperTbQueryWrapper
                .eq("site_id", franchiseeSiteTb.getSiteId())
                .ge("date", firstDate)
                .le("date", lastDate);
        List<DailyPaperTb> temp = dailyPaperTbMapper.selectList(dailyPaperTbQueryWrapper);
        if (temp == null || temp.size() < 5) {
            return null;
        }
        if (inputDecAmount == null && siteSum < 7800) {//低于这个就没必要了
            return null;
        }
        int maxDiff= 3800;
        if(siteSum>=15000&&siteSum<30000){
            maxDiff=5000;
        }else if(siteSum>=30000&&siteSum<50000){
            maxDiff=10000;
        }else if(siteSum>=50000&&siteSum<80000){
            maxDiff=15000;
        }else if(siteSum>=80000&&siteSum<120000){
            maxDiff=25000;
        }else if(siteSum>=120000){
            maxDiff=35000;
        }
        for (DailyPaperTb dailyPaperTb : temp) {
            if (!judgeExists(inputVendorId, franchiseeSiteTb.getSiteId(), dailyPaperTb.getDate())) {
                if (dailyPaperTb.getRechargeCount() > 2) {
                    double diff=Math.abs(dailyPaperTb.getVendorRechargeAmount() - siteSum);
                    if (diff < maxDiff) {
                        FaSettlementTb faSettlementTb = getFaSettlementTb(inputVendorId, franchiseeSiteTb.getSiteId(), dailyPaperTb.getDate());
                        if (faSettlementTb != null)
                            dailyDatas.add(new DailyData(dailyPaperTb,faSettlementTb));
                    }
                }
            }
            if(dailyDatas.size()>2){
                return dailyDatas;
            }
        }
        DailyPaperTb res = null;
        double minDiff = 9999999;
        for (DailyPaperTb dailyPaperTb : temp) {
            if (dailyPaperTb.getRechargeCount() > 2) {
                if (!judgeExists(inputVendorId, franchiseeSiteTb.getSiteId(), dailyPaperTb.getDate())) {
                    if (Math.abs(dailyPaperTb.getVendorRechargeAmount() - siteSum) < minDiff) {
                        minDiff = Math.abs(dailyPaperTb.getVendorRechargeAmount() - siteSum);
                        res = dailyPaperTb;
                    }
                }

            }
        }
        if(res==null){
            return dailyDatas;
        }
       FaSettlementTb tmp= getFaSettlementTb(inputVendorId, franchiseeSiteTb.getSiteId(), res.getDate());
        if(tmp!=null){
            dailyDatas.add(new DailyData(res,tmp ));
        }
        return dailyDatas;
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
    private TodayData getTodayIncome(FranchiseeSiteTb franchiseeSiteTb, Integer inputVendorId) throws ParseException {
        //QueryWrapper<VendorProfitSharingTb> vendorProfitSharingTbQueryWrapper = new QueryWrapper();
        QueryWrapper<SiteLatestDataTb> siteLatestDataTbQueryWrapper = new QueryWrapper<>();
        long time = System.currentTimeMillis();
        String lastDay = SIMPLE_DATE_FORMAT.format(new Date(time - 24 * 60 * 60 * 1000));
        List<FaSettlementTb> faSettlementTbs = faSettlementTbMapper.selectList(new QueryWrapper<FaSettlementTb>()
                .eq("own_id", inputVendorId)
                .eq("date", lastDay)
                .eq("site_id", franchiseeSiteTb.getSiteId()));
        if (CollectionUtils.isEmpty(faSettlementTbs)) {
            return null;
        }
        FaSettlementTb faSettlementTb = faSettlementTbs.get(0);
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String sDate = "";
        if (hour < 6) {
            sDate = SIMPLE_DATE_FORMAT.format(new Date(time - 24 * 60 * 60 * 1000));//如果是凌晨需要取前一天的日期
        } else {
            sDate = SIMPLE_DATE_FORMAT.format(new Date(time));
        }
        siteLatestDataTbQueryWrapper.eq("site_id", franchiseeSiteTb.getSiteId())
                .eq("date", sDate);
        List<SiteLatestDataTb> siteLatestDataTbs = siteLatestDataTbTbMapper.selectList(siteLatestDataTbQueryWrapper);
        siteLatestDataTbs.sort((k1, k2) -> k2.getNumIndex() - k1.getNumIndex());

//        long lastDayTime = SIMPLE_DATE_FORMAT.parse(sDate).getTime() / 1000;
//        vendorProfitSharingTbQueryWrapper
//                .eq("site_id", franchiseeSiteTb.getSiteId())
//                .eq("type", 1)
//                .eq("vendor_id", inputVendorId)
//                .ge("created_at", lastDayTime)
//                .le("created_at", lastDayTime + 24 * 60 * 60);

        //获取当天income
//        List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(vendorProfitSharingTbQueryWrapper);
//        DoubleSummaryStatistics todayVendorSum = vendorProfitSharingTbs.stream().collect(Collectors.summarizingDouble(VendorProfitSharingTb::getAmount));
        if (CollectionUtils.isEmpty(siteLatestDataTbs)) {
            return null;
        }
        return new TodayData(faSettlementTb.getEarnings(), siteLatestDataTbs.get(0));
    }

}
