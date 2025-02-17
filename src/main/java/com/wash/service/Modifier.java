package com.wash.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.entity.DailyData;
import com.wash.entity.ModifierData;
import com.wash.entity.Series;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.entity.statistics.DailyPaperTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.entity.statistics.MonthPaperTb;
import com.wash.mapper.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class Modifier {

    @Autowired
    private DailyPaperTbMapper dailyPaperTbMapper;
    @Autowired
    private FaSettlementTbMapper faSettlementTbMapper;
    @Autowired
    private EnsureIncomeTbMapper ensureIncomeTbMapper;
    @Autowired
    private MonthPaperTbMapper monthPaperTbMapper;
    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;

    @Autowired
    private PayTbMapper payTbMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private VendorProfitSharingTbMapper vendorProfitSharingTbMapper;
    @Autowired
    private CommodityOrdersTbMapper commodityOrdersTbMapper;

    @Transactional
    public void delete(int vendorId,FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, List<Series> seriesList) {

        for(Series series:seriesList){
            if(CollectionUtils.isNotEmpty(series.getOrdersTbs())){
                ordersTbMapper.deleteBatchIds(series.getOrdersTbs());
            }
            payTbMapper.deleteById(series.getPayTb());
            commodityOrdersTbMapper.deleteById(series.getCommodityOrderTb());
            vendorProfitSharingTbMapper.deleteBatchIds(series.getVendorProfitSharingTbs());
        }

    }
    @Transactional
    public ModifierData update(FranchiseeTb franchiseeTb, int vendorId, DailyData dailyData, FranchiseeSiteTb franchiseeSiteTb, List<Series> resSeries) throws IOException {

        FaSettlementTb faSettlementTbRes=dailyData.getFaSettlementTb();
        ModifierData modifierData=new ModifierData(dailyData,resSeries,faSettlementTbRes.getDate(),faSettlementTbRes.getSiteId(),vendorId,franchiseeTb.getWaitWithdraw());
        modifierData.generateAmount();

        modifierDailyPaper(franchiseeSiteTb, modifierData);
        modifierFaSettlement(vendorId, faSettlementTbRes, modifierData);

        int selectMonth= modifierData.getSelectMonth();
        int totalChargeAmount=0;
        int totalChargeCount=0;
        int totalWashCount=0;
        for(int date:modifierData.getMONTH_CHARGE_MAP().keySet()){
            UpdateWrapper<MonthPaperTb> monthPaperTbUpdateWrapper=new UpdateWrapper<>();
            int chargeAmount=date==selectMonth?modifierData.getTotalChargeAmount():0;
            totalChargeAmount+=chargeAmount;
            int chargeCount=date==selectMonth?modifierData.getPayCount():0;
            totalChargeCount+=chargeCount;
            int washCount=modifierData.getMONTH_WASHCOUNT_MAP().getOrDefault(date,new AtomicInteger(0)).get();
            totalWashCount+=washCount;
            monthPaperTbUpdateWrapper
                    .eq("date",date)
                    .eq("site_id",faSettlementTbRes.getSiteId())
                    .setSql(chargeAmount!=0,"recharge_amount = recharge_amount-"+chargeAmount)
                    .setSql(chargeAmount!=0,"vendor_recharge_amount = vendor_recharge_amount-"+chargeAmount)
                    .setSql(totalChargeAmount!=0,"vendor_total_recharge_amount= vendor_total_recharge_amount-"+totalChargeAmount)
                    .setSql(chargeCount!=0,"recharge_count = recharge_count-"+chargeCount)
                    .setSql(totalChargeCount!=0,"all_third_pay_usr_count = all_third_pay_usr_count-"+totalChargeCount)
                    .setSql(totalChargeCount!=0,"total_wash_user_count = total_wash_user_count-"+totalChargeCount)
                    .setSql(totalWashCount!=0,"total_wash_count = total_wash_count-"+totalWashCount)
                    .setSql(chargeCount!=0,"wash_user_count = wash_user_count-"+chargeCount)
                    .setSql(washCount!=0,"wash_count = wash_count-"+washCount);
            if((chargeAmount+totalChargeAmount+chargeCount+totalChargeCount+totalWashCount+washCount)!=0) {
                monthPaperTbMapper.update(null, monthPaperTbUpdateWrapper);
            }
        }

       return modifierData;
    }

    private void modifierFaSettlement(int vendorId, FaSettlementTb faSettlementTbRes, ModifierData modifierData) {
        for(int date: modifierData.getDAY_INCOME_MAP().keySet()){
        UpdateWrapper<FaSettlementTb> faSettlementTbUpdateWrapper=new UpdateWrapper<>();
            int income= modifierData.getDAY_INCOME_MAP().getOrDefault(date,new AtomicInteger(0)).get();
            faSettlementTbUpdateWrapper
                    .eq("own_id", vendorId)
                    .eq("date",date)
                    .eq("site_id", faSettlementTbRes.getSiteId())
                    .setSql("earnings=earnings-"+income);
            faSettlementTbMapper.update(null,faSettlementTbUpdateWrapper);
        }
    }

    private void modifierDailyPaper(FranchiseeSiteTb franchiseeSiteTb, ModifierData modifierData) {
        for (int date : modifierData.getDAY_WASHCOUNT_MAP().keySet()) {//只管日washcount
            UpdateWrapper<DailyPaperTb> dailyPaperTbUpdateWrapper = new UpdateWrapper<>();
            int washCount = modifierData.getDAY_WASHCOUNT_MAP().getOrDefault(date, new AtomicInteger(0)).get();
            int washDuration = modifierData.getDAY_WASHTIME_MAP().getOrDefault(date, new AtomicInteger(0)).get();
            dailyPaperTbUpdateWrapper.eq("site_id", franchiseeSiteTb.getSiteId())
                    .eq("date",date)
                    .setSql(washCount!=0,"wash_user_count = wash_user_count-" + washCount)
                    .setSql(washCount!=0,"wash_count = wash_count-" + washCount)
                    .setSql(washDuration!=0,"wash_duration = wash_duration-" + washDuration);
            dailyPaperTbMapper.update(null, dailyPaperTbUpdateWrapper);
        }

        //当天
        int rechargeAmount = modifierData.getTotalChargeAmount();
        int rechargeCount = modifierData.getPayCount();
        UpdateWrapper<DailyPaperTb> selectWrapper = new UpdateWrapper<>();
        selectWrapper
                .eq("site_id", franchiseeSiteTb.getSiteId())
                .eq("date", modifierData.getSelectDate())
                .setSql("recharge_amount = recharge_amount-" + rechargeAmount)
                .setSql("vendor_recharge_amount = vendor_recharge_amount-" + rechargeAmount)
                .setSql("recharge_count = recharge_count-" + rechargeCount)
                .setSql("recharge_user_count = recharge_user_count-" + rechargeCount);
        dailyPaperTbMapper.update(null,selectWrapper);
        //当月
        UpdateWrapper<DailyPaperTb> monthWrapper = new UpdateWrapper<>();
        monthWrapper
                .eq("site_id", franchiseeSiteTb.getSiteId())
                .ge("date", modifierData.getSelectDate())
                .le("date", modifierData.getMonthLastDate())
                .setSql("cur_month_pay_amount = cur_month_pay_amount-"+rechargeAmount)
                .setSql("vendor_cur_month_pay_amount = vendor_cur_month_pay_amount-"+rechargeAmount);
        dailyPaperTbMapper.update(null,monthWrapper);

        //累计至今日
        UpdateWrapper<DailyPaperTb> curWrapper = new UpdateWrapper<>();
        curWrapper
                .eq("site_id", franchiseeSiteTb.getSiteId())
                .ge("date", modifierData.getSelectDate())
                .le("date", modifierData.getCurDate())
                .setSql("recharge_amount_total = recharge_amount_total-"+rechargeAmount)
                .setSql("vendor_recharge_amount_total = vendor_recharge_amount_total-"+rechargeAmount);
        dailyPaperTbMapper.update(null,curWrapper);

    }
}
