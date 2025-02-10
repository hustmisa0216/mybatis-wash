package com.wash.entity;

import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.PayTb;
import com.wash.entity.data.VendorProfitSharingTb;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/10
 * @Description
 */
@Data
public class ModifierData {

    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyyMMdd"));
    private List<Series> seriesList;

    public ModifierData(List<Series> seriesList){
        this.seriesList=seriesList;
    }
    private int totalChargeAmount;//充值是当天的
    private int totalIncome;
    private Map<String,AtomicInteger> DAY_INCOME_MAP=new HashMap<>();
    private Map<String, AtomicInteger> MONTH_INCOME_MAP=new HashMap<>();

    private Map<String,AtomicInteger> DAY_CHARGEAMOUNT_MAP=new HashMap<>();

    private Map<String,AtomicInteger> DAY_WASHCOUNT_MAP=new HashMap<>();
    private Map<String, AtomicInteger> MONTH_WASHCOUNT_MAP=new HashMap<>();

    private Map<String ,AtomicInteger> DAY_WASHTIME_MAP=new HashMap<>();
    private int payCount=0;
    private int newPayCount=0;

    private int first_wash_user_count=0;

    public void generateAmount(){

        for(Series series:seriesList){
            totalChargeAmount+=series.getPayTb().getAmount();
            payCount+=1;
            first_wash_user_count+=1;
            for(VendorProfitSharingTb vendorProfitSharingTb:series.getVendorProfitSharingTbs()){
                totalIncome+=vendorProfitSharingTb.getAmount();
                DAY_INCOME_MAP.computeIfAbsent(vendorProfitSharingTb.getDate(), k -> new AtomicInteger(0)).addAndGet(vendorProfitSharingTb.getAmount());
                MONTH_INCOME_MAP.computeIfAbsent(vendorProfitSharingTb.getDateMonth(), k -> new AtomicInteger(0)).addAndGet(vendorProfitSharingTb.getAmount());
            }


            DAY_CHARGEAMOUNT_MAP.put(series.getPayTb().getDate(),new AtomicInteger(series.getPayTb().getAmount()));
            DAY_WASHCOUNT_MAP.put(series.getPayTb().getDate(),new AtomicInteger(0));
            DAY_WASHTIME_MAP.put(series.getPayTb().getDate(),new AtomicInteger(0));

            for(OrdersTb ordersTb:series.getOrdersTbs()){
                DAY_WASHCOUNT_MAP.computeIfAbsent(ordersTb.getDate(),k->new AtomicInteger(0)).addAndGet(1);
                MONTH_WASHCOUNT_MAP.computeIfAbsent(ordersTb.getDateMonth(),k->new AtomicInteger(0)).addAndGet(1);
                DAY_WASHTIME_MAP.computeIfAbsent(ordersTb.getDate(),k->new AtomicInteger(0)).addAndGet((int) (ordersTb.getEndAt()-ordersTb.getStartAt()));
            }
        }
        newPayCount=payCount/2+1;
    }

}
