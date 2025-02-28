package com.charge.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<Integer, Map<Integer, Cdata>> date_map=new HashMap<>();
    private Map<Integer, Map<Integer, Cdata>> month_map=new HashMap<>();

    private int amount;//总的


    public CharModifier(List<CharEntity> resEnetities) {
        this.charEntityList =resEnetities;
    }

    public void calculate(Integer inputVendorId) {

        for(CharEntity charEntity:charEntityList){
            Pay pay=charEntity.getPay();
            amount+=pay.getAmount();
            String dates=SIMPLE_DATE_FORMAT.format(new Date(charEntity.getPay().getCreatedAt()));
            int date=Integer.valueOf(dates);
            date_map.computeIfAbsent(pay.getSiteId(),k->new HashMap<>())
                    .computeIfAbsent(date,i->new Cdata())
                            .getAmount().addAndGet(pay.getAmount());
            date_map.computeIfAbsent(pay.getSiteId(),k->new HashMap<>())
                    .computeIfAbsent(date,i->new Cdata())
                    .getRe_count().addAndGet(1);
            String months=dates.substring(0,6);
            int month=Integer.valueOf(months);

            month_map.computeIfAbsent(pay.getSiteId(),k->new HashMap<>())
                    .computeIfAbsent(month,i->new Cdata())
                    .getAmount().addAndGet(pay.getAmount());
            month_map.computeIfAbsent(pay.getSiteId(),k->new HashMap<>())
                    .computeIfAbsent(month,i->new Cdata())
                    .getRe_count().incrementAndGet();

            for(CommodityOrderProfitSharing commodityOrderProfitSharing: charEntity.getCommodityOrderProfitSharingList()){
                String incomeDates=SIMPLE_DATE_FORMAT.format(new Date(commodityOrderProfitSharing.getCreatedAt()));
                int incomeDate=Integer.valueOf(incomeDates);
                int income_site=commodityOrderProfitSharing.getSiteId();
                date_map.computeIfAbsent(income_site,k->new HashMap<>())
                        .computeIfAbsent(incomeDate,i->new Cdata())
                        .getIncome().addAndGet(commodityOrderProfitSharing.getAmount());
            }

            for(ChargeOrder chargeOrder:charEntity.getChargeOrders()){
                String chargeDates=SIMPLE_DATE_FORMAT.format(new Date(chargeOrder.getCreatedAt()));
                int chargeDate=Integer.valueOf(chargeDates);
                int chageMonth=Integer.valueOf(chargeDates.substring(0,6));
                month_map.computeIfAbsent(chargeOrder.getSiteId(),k->new HashMap<>())
                        .computeIfAbsent(chageMonth,i->new Cdata())
                        .getCharge_count().incrementAndGet();
                date_map.computeIfAbsent(chargeOrder.getSiteId(),k->new HashMap<>())
                        .computeIfAbsent(chargeDate,i->new Cdata())
                        .getConsume().addAndGet(chargeOrder.getPaymentBalance());
                date_map.computeIfAbsent(chargeOrder.getSiteId(),k->new HashMap<>())
                        .computeIfAbsent(chargeDate,i->new Cdata())
                        .getCharge_count().incrementAndGet();
            }

        }

    }
}
