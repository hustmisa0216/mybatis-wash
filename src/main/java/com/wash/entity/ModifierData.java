package com.wash.entity;

import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.VendorProfitSharingTb;
import com.wash.entity.statistics.FaSettlementTb;
import lombok.Data;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/10
 * @Description
 */
@Data
public class ModifierData {
   private static  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyyMMdd"));
    private List<Series> seriesList;
    private int selectDate;
    private int vendorId;
    private int siteId;
    private DailyData dailyData;
    private int totalChargeAmount;//充值是当天的
    private int totalIncome;
    private Map<Integer,AtomicInteger> DAY_INCOME_MAP=new TreeMap<>();
    private Map<Integer,AtomicInteger> DAY_WASHCOUNT_MAP=new TreeMap<>();
    private Map<Integer, AtomicInteger> MONTH_WASHCOUNT_MAP=new TreeMap<>();
    private Map<Integer, AtomicInteger> MONTH_CHARGE_MAP=new TreeMap<>();
    private Map<Integer ,AtomicInteger> DAY_WASHTIME_MAP=new TreeMap<>();
    private int payCount=0;
    private int newPayCount=0;
    private int first_wash_user_count=0;
    private int washCount=0;
    private int curDate;
    private int monthLastDate;
    private int selectMonth;
    private int curMonth;
    private String key;




    public ModifierData(DailyData dailyData, List<Series> seriesList, int date, int siteId, int vendorId){
        this.dailyData=dailyData;
        this.seriesList=seriesList;
        this.selectDate=date;
        this.vendorId=vendorId;this.siteId=siteId;
    }
    public void generateAmount() throws IOException {

        String selectDates=selectDate+"";
        LocalDate startDate = LocalDate.of(Integer.valueOf(selectDates.substring(0,4)), Integer.valueOf(selectDates.substring(4,6)), Integer.valueOf(selectDates.substring(6,8)));
        LocalDate localCurDate=LocalDate.now();
        curDate=Integer.valueOf(localCurDate.format(formatter));
        curMonth=Integer.valueOf((""+curDate).substring(0,6));
        selectMonth=Integer.valueOf((selectDates).substring(0,6));
        for(Series series:seriesList){
            totalChargeAmount+=series.getPayTb().getAmount();
            payCount+=1;
            first_wash_user_count+=1;
            for(VendorProfitSharingTb vendorProfitSharingTb:series.getVendorProfitSharingTbs()){
                totalIncome+=vendorProfitSharingTb.getAmount();
                DAY_INCOME_MAP.computeIfAbsent(vendorProfitSharingTb.getDate(), k -> new AtomicInteger(0)).addAndGet(vendorProfitSharingTb.getAmount());
            }

            LocalDate tmp= LocalDate.of(Integer.valueOf(selectDates.substring(0,4)), Integer.valueOf(selectDates.substring(4,6)), Integer.valueOf(selectDates.substring(6,8)));
            while(tmp.isBefore(localCurDate)){
                if(tmp.getYear()==startDate.getYear()&&tmp.getMonth()==startDate.getMonth()){
                    monthLastDate=Integer.valueOf(tmp.format(formatter));
                }
                tmp=tmp.plusDays(1);
            }

            for(OrdersTb ordersTb:series.getOrdersTbs()){
                washCount+=1;
                DAY_WASHCOUNT_MAP.computeIfAbsent(Integer.valueOf(ordersTb.getDate()),k->new AtomicInteger(0)).addAndGet(1);
                MONTH_WASHCOUNT_MAP.computeIfAbsent(ordersTb.getDateMonth(),k->new AtomicInteger(0)).addAndGet(1);
                DAY_WASHTIME_MAP.computeIfAbsent(Integer.valueOf(ordersTb.getDate()),k->new AtomicInteger(0)).addAndGet(ordersTb.getComboTime());
            }
        }
        while(!startDate.isAfter(localCurDate)){
            String date=startDate.format(formatter).substring(0,6);
            MONTH_CHARGE_MAP.put(Integer.valueOf(date),new AtomicInteger(totalChargeAmount));
            startDate=startDate.plusMonths(1);
        }


        int allPayCount=dailyData.getDailyPaperTb().getRechargeCount();
        int dayRechargeAmount=dailyData.getDailyPaperTb().getRechargeAmount();
        int allIn=dailyData.getFaSettlementTb().getEarnings();
        this.key=vendorId+"-"+siteId+"-"+allPayCount+"-"+dayRechargeAmount+"-"+allIn+"-"+payCount+"-"+totalChargeAmount+"-"+totalIncome;
        newPayCount=payCount/2+1;

    }

    public static void main(String[] args){

        String selectDate="20240105";
        LocalDate startDate = LocalDate.of(Integer.valueOf(selectDate.substring(0,4)), Integer.valueOf(selectDate.substring(4,6)), Integer.valueOf(selectDate.substring(6,8)));
        LocalDate curDate=LocalDate.now();


        List<String> res=new ArrayList<>();
        while(!startDate.isAfter(curDate)){
            String date=startDate.format(formatter).substring(0,6);
            startDate=startDate.plusMonths(1);
            res.add(date);
        }
        System.out.println(res);
    }





}
