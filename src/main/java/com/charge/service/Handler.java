package com.charge.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.charge.entity.*;
import com.charge.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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


    public void de(int vendorId,CharModifier charModifier){
        for(CharEntity charEntity:charModifier.getCharEntityList()){
            payMapper.deleteById(charEntity.getPay());
            commodityOrderMapper.deleteById(charEntity.getCommodityOrder());
            vendorProfitSharingMapper.deleteBatchIds(charEntity.getVendorProfitSharingList());
        }
    }

    public void update(int vendorId,CharModifier charModifier){

        Map<Integer, Map<Integer, Cdata>> s_date_map = charModifier.getDate_map();
        Map<Integer, Map<Integer, Cdata>> s_month_map =charModifier.getMonth_map();
       for(int siteId:s_date_map.keySet()){
           Map<Integer,Cdata> date_map=s_date_map.get(siteId);

           for(int date:date_map.keySet()){
               Cdata cdata=date_map.get(date);
               UpdateWrapper<StatementDaily> statementDailyUpdateWrapper=new UpdateWrapper<>();

               if(cdata.getAmount().get()!=0) {
                   statementDailyUpdateWrapper.eq("site_id", siteId)
                           .eq("date", date)
                           .setSql("recharge_amount = recharge_amount -"+cdata.getAmount())
                           .setSql("recharge_times = recharge_times-"+cdata.getRe_count())
                           .setSql("recharge_user_count = recharge_user_count-"+cdata.getRe_count())
                           .setSql("cur_month_recharge_user_count = cur_month_recharge_user_count-"+cdata.getRe_count());

                   //statementDailyMapper.update(null,statementDailyUpdateWrapper);

               }
               statementDailyUpdateWrapper.clear();

               if(cdata.getIncome().get()!=0){

               }

           }
       }
       for(int siteId:s_month_map.keySet()){
           Map<Integer,Cdata> month_map=s_month_map.get(siteId);
       }
    }

}
