package com.charge.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/27
 * @Description
 */
@Data
public class CharModifier {
    @JSONField(serialize = false)

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @JSONField(serialize = false)

    private List<CharEntity> charEntityList;
    private Map<Integer, TreeMap<Integer, Cdata>> date_map=new HashMap<>();
    private Map<Integer, TreeMap<Integer, Cdata>> month_map=new HashMap<>();

    private int amount;//总的
    @JSONField(serialize = false)
    private List<Pay> pays;

    private int selectDate;
    private int allCount;
    private int secCount;
    private int allAmount;
    private int before;
    private int after;


    public CharModifier(List<CharEntity> resEnetities) {
        this.charEntityList =resEnetities;
    }

    public CharModifier(Integer selectDate, List<Pay> pays, List<CharEntity> resEnetities) {
        this.selectDate=selectDate;
        this.charEntityList =resEnetities;
        this.pays=pays;
    }

    public void calculate(Integer inputVendorId) {

        this.allCount=pays.size();
        this.allAmount=pays.stream().mapToInt(Pay::getAmount).sum();
        this.secCount=charEntityList.size();

        for(CharEntity charEntity:charEntityList){
            Pay pay=charEntity.getPay();
            amount+=pay.getAmount();
            String dates=SIMPLE_DATE_FORMAT.format(new Date(charEntity.getPay().getCreatedAt()*1000));
            int date=Integer.valueOf(dates);
            date_map.computeIfAbsent(pay.getSiteId(),k->new TreeMap<>())
                    .computeIfAbsent(date,i->new Cdata())
                            .getAmount().addAndGet(pay.getAmount());
            date_map.computeIfAbsent(pay.getSiteId(),k->new TreeMap<>())
                    .computeIfAbsent(date,i->new Cdata())
                    .getRe_count().addAndGet(1);
            String months=dates.substring(0,6);
            int month=Integer.valueOf(months);

            month_map.computeIfAbsent(pay.getSiteId(),k->new TreeMap<>())
                    .computeIfAbsent(month,i->new Cdata())
                    .getAmount().addAndGet(pay.getAmount());
            month_map.computeIfAbsent(pay.getSiteId(),k->new TreeMap<>())
                    .computeIfAbsent(month,i->new Cdata())
                    .getRe_count().incrementAndGet();

            for(CommodityOrderProfitSharing commodityOrderProfitSharing: charEntity.getCommodityOrderProfitSharingList()){
                String incomeDates=SIMPLE_DATE_FORMAT.format(new Date(commodityOrderProfitSharing.getCreatedAt()*1000));
                int incomeDate=Integer.valueOf(incomeDates);
                int income_site=commodityOrderProfitSharing.getSiteId();
                date_map.computeIfAbsent(income_site,k->new TreeMap<>())
                        .computeIfAbsent(incomeDate,i->new Cdata())
                        .getIncome().addAndGet(commodityOrderProfitSharing.getAmount());
                if(commodityOrderProfitSharing.getSubType()==1){
                    date_map.computeIfAbsent(income_site,k->new TreeMap<>())
                            .computeIfAbsent(incomeDate,i->new Cdata())
                            .getPayment().addAndGet(commodityOrderProfitSharing.getAmount());
                }else if(commodityOrderProfitSharing.getSubType()==2){
                    date_map.computeIfAbsent(income_site,k->new TreeMap<>())
                            .computeIfAbsent(incomeDate,i->new Cdata())
                            .getBalance().addAndGet(commodityOrderProfitSharing.getAmount());
                }

            }

            for(ChargeOrder chargeOrder:charEntity.getChargeOrders()){
                String chargeDates=SIMPLE_DATE_FORMAT.format(new Date(chargeOrder.getCreatedAt()*1000));
                int chargeDate=Integer.valueOf(chargeDates);
                int chageMonth=Integer.valueOf(chargeDates.substring(0,6));

                month_map.computeIfAbsent(chargeOrder.getSiteId(),k->new TreeMap<>())
                        .computeIfAbsent(chageMonth,i->new Cdata())
                        .getCharge_count().incrementAndGet();
                date_map.computeIfAbsent(chargeOrder.getSiteId(),k->new TreeMap<>())
                        .computeIfAbsent(chargeDate,i->new Cdata())
                        .getConsume().addAndGet(chargeOrder.getPaymentBalance());
                date_map.computeIfAbsent(chargeOrder.getSiteId(),k->new TreeMap<>())
                        .computeIfAbsent(chargeDate,i->new Cdata())
                        .getCharge_count().incrementAndGet();
                date_map.computeIfAbsent(chargeOrder.getSiteId(),k->new TreeMap<>())
                        .computeIfAbsent(chargeDate,i->new Cdata())
                        .getTimes().addAndGet(chargeOrder.getChargingMinutes());
            }

        }

    }

    public String buildKey() {
        return selectDate+"||("+allCount+"-"+allAmount+")||("+secCount+"-"+amount+")||("+Math.ceil(before/100)+"-"+Math.ceil(after/100) +")";
    }
}
