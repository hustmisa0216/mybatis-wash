package com.charge.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.charge.entity.*;
import com.charge.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/28
 * @Description
 */
@Service
public class Handler {


    @Autowired
    private PayMapper payMapper;
    @Autowired
    private CommodityOrderMapper commodityOrderMapper;

    @Autowired
    private VendorProfitSharingMapper vendorProfitSharingMapper;

    @Autowired
    private VendorMapper vendorMapper;
    @Autowired
    private StatementDailyMapper statementDailyMapper;
    @Autowired
    private StatementMonthlyMapper statementMonthlyMapper;
    @Autowired
    private StatementSiteMapper statementSiteMapper;
    @Autowired
    private StatementVendorDailyMapper statementVendorDailyMapper;

    @Autowired
    private ChargeOrderMapper chargeOrderMapper;

    public void de(int vendorId, CharModifier charModifier) {
        for (CharEntity charEntity : charModifier.getCharEntityList()) {
            payMapper.deleteById(charEntity.getPay());
            commodityOrderMapper.deleteById(charEntity.getCommodityOrder());
            chargeOrderMapper.deleteBatchIds(charEntity.getChargeOrders());
            vendorProfitSharingMapper.deleteBatchIds(charEntity.getVendorProfitSharingList());
        }
    }

    public void update(int vendorId, CharModifier charModifier) {

        Map<Integer, TreeMap<Integer, Cdata>> s_date_map = charModifier.getDate_map();
        Map<Integer, TreeMap<Integer, Cdata>> s_month_map = charModifier.getMonth_map();
        int allAmount=0;
        int income=0;
        for (int siteId : s_date_map.keySet()) {
            TreeMap<Integer, Cdata> date_map = s_date_map.get(siteId);

            int amount = 0;
            int charge_count = 0;
            int consume = 0;
            int duration = 0;
            int payment = 0;
            int balance = 0;
            for (int date : date_map.keySet()) {
                Cdata cdata = date_map.get(date);
                UpdateWrapper<StatementsDaily> statementDailyUpdateWrapper = new UpdateWrapper<>();

                if (cdata.getAmount().get() != 0) {
                    statementDailyUpdateWrapper.eq("site_id", siteId)
                            .eq("date", date)
                            .setSql("recharge_amount = recharge_amount -" + cdata.getAmount().get())
                            .setSql("recharge_times = recharge_times-" + cdata.getRe_count().get())
                            .setSql("recharge_user_count = recharge_user_count-" + cdata.getRe_count().get())
                            .setSql("cur_month_recharge_user_count = cur_month_recharge_user_count-" + cdata.getRe_count().get());
                    amount += cdata.getAmount().get();
                    statementDailyMapper.update(null,statementDailyUpdateWrapper);
                }

                statementDailyUpdateWrapper.clear();

                if (cdata.getAmount().get() != 0) {
                    statementDailyUpdateWrapper.eq("site_id", siteId)
                            .ge("date", date)
                            .setSql("recharge_amount_total = recharge_amount_total-" + cdata.getAmount().get());
                    statementDailyMapper.update(null,statementDailyUpdateWrapper);
                }
                statementDailyUpdateWrapper.clear();

                if (cdata.getCharge_count().get() != 0) {
                    statementDailyUpdateWrapper.eq("site_id", siteId)
                            .eq("date", date)
                            .setSql("charge_times = charge_times-" + cdata.getCharge_count().get())
                            .setSql("cur_month_charge_user_count = cur_month_charge_user_count-" + cdata.getCharge_count().get())
                            .setSql("charge_duration = charge_duration-" + cdata.getTimes().get())
                            .setSql("charge_consume_amount = charge_consume_amount-" + cdata.getConsume().get());
                    charge_count += cdata.getCharge_count().get();
                    consume += cdata.getConsume().get();
                    duration += cdata.getTimes().get();

                    statementDailyMapper.update(null,statementDailyUpdateWrapper);
                }

                UpdateWrapper<StatementsVendorDaily> statementVendorDailyUpdateWrapper = new UpdateWrapper<>();
                if (cdata.getIncome().get() != 0) {
                    statementVendorDailyUpdateWrapper.eq("vendor_id", vendorId)
                            .eq("site_id", siteId)
                            .eq("date", date)
                            .setSql("profit_sharing_income_amount = profit_sharing_income_amount-" + cdata.getIncome().get());
                   statementVendorDailyMapper.update(null,statementVendorDailyUpdateWrapper);
                    statementVendorDailyUpdateWrapper.clear();
                    statementVendorDailyUpdateWrapper.eq("vendor_id", vendorId)
                            .eq("site_id", siteId)
                            .ge("date", date)
                            .setSql("profit_sharing_total_amount = profit_sharing_total_amount-" + cdata.getIncome().get());
                     statementVendorDailyMapper.update(null,statementVendorDailyUpdateWrapper);
                    payment += cdata.getPayment().get();
                    balance += cdata.getBalance().get();
                }
            }

            UpdateWrapper<StatementsSite> statementSiteUpdateWrapper = new UpdateWrapper<>();
            statementSiteUpdateWrapper.eq("site_id", siteId)
                    .setSql("recharge_amount = recharge_amount-" + amount)
                    .setSql("charge_times = charge_times-" + charge_count)
                    .setSql("charge_duration = charge_duration-" + duration)
                    .setSql("charge_consume_amount = charge_consume_amount-" + consume)
                    .setSql("profit_sharing_payment_amount = profit_sharing_payment_amount-" + payment)
                    .setSql("profit_sharing_balance_amount = profit_sharing_balance_amount-" + balance);
            statementSiteMapper.update(null,statementSiteUpdateWrapper);
            allAmount+=amount;
            income+=payment+balance;
        }


        for (int siteId : s_month_map.keySet()) {
            TreeMap<Integer, Cdata> month_map = s_month_map.get(siteId);
            for (int date : month_map.keySet()) {
                Cdata cdata = month_map.get(date);
                UpdateWrapper<StatementsMonthly> statementsMonthlyUpdateWrapper = new UpdateWrapper<>();

                if (cdata.getAmount().get() != 0) {
                    statementsMonthlyUpdateWrapper.eq("site_id", siteId)
                            .eq("date", date)
                            .setSql("recharge_amount = recharge_amount -" + cdata.getAmount().get())
                            .setSql("recharge_times = recharge_times-" + cdata.getRe_count().get())
                            .setSql("recharge_user_count = recharge_user_count-" + cdata.getRe_count().get());
                       statementMonthlyMapper.update(null,statementsMonthlyUpdateWrapper);
                }

                statementsMonthlyUpdateWrapper.clear();

                if (cdata.getAmount().get() != 0) {
                    statementsMonthlyUpdateWrapper.eq("site_id", siteId)
                            .ge("date", date)
                            .setSql("recharge_amount_total = recharge_amount_total-" + cdata.getAmount().get())
                            .setSql("recharge_user_count_total = recharge_user_count_total-" + cdata.getRe_count().get());

                        statementMonthlyMapper.update(null,statementsMonthlyUpdateWrapper);
                }
                statementsMonthlyUpdateWrapper.clear();

                if (cdata.getCharge_count().get() != 0) {
                    statementsMonthlyUpdateWrapper.eq("site_id", siteId)
                            .eq("date", date)
                            .setSql("charge_times = charge_times-" + cdata.getCharge_count().get())
                            .setSql("charge_duration = charge_duration-" + cdata.getTimes().get())
                            .setSql("charge_consume_amount = charge_consume_amount-" + cdata.getConsume().get());
                     statementMonthlyMapper.update(null,statementsMonthlyUpdateWrapper);
                    statementsMonthlyUpdateWrapper.clear();

                    statementsMonthlyUpdateWrapper.eq("site_id", siteId)
                            .ge("date", date)
                            .setSql("charge_times_total = charge_times_total-" + cdata.getCharge_count().get());
                    statementMonthlyMapper.update(null,statementsMonthlyUpdateWrapper);

                }
            }
        }


        UpdateWrapper<Vendor> vendorUpdateWrapper=new UpdateWrapper<>();
        vendorUpdateWrapper.eq("id",vendorId)
                .setSql("profit_sharing_amount = profit_sharing_amount-"+allAmount)
                .setSql("undrawn_amount = undrawn_amount-"+allAmount);
         vendorMapper.update(null,vendorUpdateWrapper);

    }

}