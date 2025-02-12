package com.wash.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.wash.entity.ModifierData;
import com.wash.entity.Series;
import com.wash.entity.data.CommodityOrdersTb;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.PayTb;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.entity.statistics.DailyPaperTb;
import com.wash.entity.statistics.EnsureIncomeTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.entity.statistics.MonthPaperTb;
import com.wash.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void delete(int vendorId,FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, List<Series> seriesList) {

        for(Series series:seriesList){
            payTbMapper.deleteById(series.getPayTb());
            ordersTbMapper.deleteBatchIds(series.getOrdersTbs());
            commodityOrdersTbMapper.deleteById(series.getCommodityOrderTb());
            vendorProfitSharingTbMapper.deleteBatchIds(series.getVendorProfitSharingTbs());
        }

    }
    public void update(int vendorId, FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, List<Series> resSeries) throws IOException {

        ModifierData modifierData=new ModifierData(faSettlementTbRes,resSeries,faSettlementTbRes.getDate(),faSettlementTbRes.getSiteId(),vendorId);
        modifierData.generateAmount();

        for(String date:modifierData.getDAY_WASHCOUNT_MAP().keySet()){
        UpdateWrapper<DailyPaperTb> dailyPaperTbUpdateWrapper=new UpdateWrapper<>();
            int washCount=modifierData.getDAY_WASHCOUNT_MAP().getOrDefault(date,new AtomicInteger(0)).get();
            int washDuration=modifierData.getDAY_WASHTIME_MAP().getOrDefault(date,new AtomicInteger(0)).get();
            int rechargeCount= StringUtils.equals(faSettlementTbRes.getDate()+"",date)?resSeries.size():0;
            int rechargeAmount=StringUtils.equals(faSettlementTbRes.getDate()+"",date)?modifierData.getTotalChargeAmount():0;//当天的
            int monthTotalRecharege=modifierData.getDAY_CHARGEAMOUNT_MAP().getOrDefault(date,new AtomicInteger(0)).get();

            dailyPaperTbUpdateWrapper
                    .eq("site_id",franchiseeSiteTb.getSiteId())
                    .eq("date",date)
                    .setSql("wash_user_count = wash_user_count-"+washCount)
                    .setSql("wash_count = wash_count-"+washCount)
                    .setSql("wash_duration = wash_duration-"+washDuration)
                    .setSql("recharge_user_count = recharge_user_count-"+rechargeCount)
                    .setSql("recharge_amount_total = recharge_amount_total-"+rechargeAmount)
                    .setSql("vendor_recharge_amount_total = vendor_recharge_amount_total-"+rechargeAmount)
                    .setSql("cur_month_pay_amount = cur_month_pay_amount-"+monthTotalRecharege)
                    .setSql("vendor_cur_month_pay_amount = vendor_cur_month_pay_amount-"+monthTotalRecharege);
            dailyPaperTbMapper.update(null,dailyPaperTbUpdateWrapper);
        }

        for(String date:modifierData.getDAY_INCOME_MAP().keySet()){
        UpdateWrapper<FaSettlementTb> faSettlementTbUpdateWrapper=new UpdateWrapper<>();
            int income=modifierData.getDAY_INCOME_MAP().getOrDefault(date,new AtomicInteger(0)).get();
            faSettlementTbUpdateWrapper
                    .eq("own_id",vendorId)
                    .eq("date",date)
                    .eq("site_id",faSettlementTbRes.getSiteId())
                    .setSql("earnings=earnings-"+income);
            faSettlementTbMapper.update(null,faSettlementTbUpdateWrapper);
        }

        String curMonth=(modifierData.getFaSettlementTb().getDate()+"").substring(0,6);
        int totalChargeAmount=0;
        int totalChargeCount=0;
        int totalWashCount=0;
        for(String date:modifierData.getMONTH_CHARGE_MAP().keySet()){
            UpdateWrapper<MonthPaperTb> monthPaperTbUpdateWrapper=new UpdateWrapper<>();
            int chargeAmount=StringUtils.equals(date,curMonth)?modifierData.getTotalChargeAmount():0;
            totalChargeAmount+=chargeAmount;
            int chargeCount=StringUtils.equals(date,curMonth)?modifierData.getPayCount():0;
            totalChargeCount+=chargeCount;
            int washCount=modifierData.getMONTH_WASHCOUNT_MAP().getOrDefault(date,new AtomicInteger(0)).get();
            totalWashCount+=washCount;
            monthPaperTbUpdateWrapper
                    .eq("date",date)
                    .eq("site_id",faSettlementTbRes.getSiteId())
                    .setSql("vendor_recharge_amount = vendor_recharge_amount-"+chargeAmount)
                    .setSql("vendor_total_recharge_amount= vendor_total_recharge_amount-"+totalChargeAmount)
                    .setSql("recharge_count = recharge_count-"+chargeCount)
                    .setSql("all_third_pay_usr_count = all_third_pay_usr_count-"+totalChargeCount)
                    .setSql("total_wash_user_count = total_wash_user_count-"+chargeCount)
                    .setSql("total_wash_count = total_wash_count-"+totalWashCount)
                    .setSql("wash_user_count = wash_user_count-"+chargeCount)
                    .setSql("wash_count = wash_count-"+washCount);
            monthPaperTbMapper.update(null,monthPaperTbUpdateWrapper);
        }


        UpdateWrapper<FranchiseeTb> franchiseeTbUpdateWrapper=new UpdateWrapper<>();
        franchiseeTbUpdateWrapper.eq("id",vendorId)
                .setSql("settled_amount = settled_amount-"+modifierData.getTotalIncome())
                .setSql("wait_withdraw = wait_withdraw-"+modifierData.getTotalIncome())
                .setSql("stmt_recharge_amount = stmt_recharge_amount-"+modifierData.getTotalChargeAmount())
                .setSql("stmt_profit_amount = stmt_profit_amount-"+modifierData.getTotalChargeAmount());
       String v= franchiseeTbUpdateWrapper.getCustomSqlSegment();
       franchiseeTbMapper.update(null,franchiseeTbUpdateWrapper);

        System.out.println(v);
    }
}
