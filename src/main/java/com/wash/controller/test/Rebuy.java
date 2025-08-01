package com.wash.controller.test;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.charge.entity.Pay;
import com.wash.entity.data.CommodityOrdersTb;
import com.wash.entity.data.CommodityTb;
import com.wash.entity.data.PayTb;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.mapper.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/7/7
 * @Description
 */
@RestController

public class Rebuy {

    @Autowired
    private FranchiseeSiteTbMapper franchiseeSiteTbMapper;

    @Autowired
    private CommodityOrdersTbMapper commodityOrdersTbMapper;

    @Autowired
    private PayTbMapper payTbMapper;

    @Autowired
    private CommodityTbMapper commodityTbMapper;


    @GetMapping("rebuy/query")
    public ResponseEntity<String> dec(
            @RequestParam(value = "own",required = true) Integer own){

        QueryWrapper<FranchiseeSiteTb> queryWrapper=new QueryWrapper<FranchiseeSiteTb>();
        queryWrapper.eq("own_id",own);
        List<FranchiseeSiteTb> franchiseeSiteTbs = franchiseeSiteTbMapper.selectList(queryWrapper);

        QueryWrapper<PayTb> payTbQueryWrapper   = new QueryWrapper<>();
        payTbQueryWrapper.in("site_id",franchiseeSiteTbs.stream().map(FranchiseeSiteTb::getSiteId).collect(Collectors.toList()));
        //payTbQueryWrapper.eq("status", 1);
        List<PayTb> payTbs = payTbMapper.selectList(payTbQueryWrapper);

        Map<Integer,List<PayTb>> payTbMap = payTbs.stream().collect(Collectors.groupingBy(PayTb::getUid));

        Iterator<Map.Entry<Integer, List<PayTb>>> iterator = payTbMap.entrySet().iterator();
        AtomicInteger refundCount=new AtomicInteger();
        AtomicInteger refundAmount=new AtomicInteger();
        AtomicInteger buyCount=new AtomicInteger();
        AtomicInteger manyBuyCount=new AtomicInteger();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<PayTb>> entry = iterator.next();
            Integer uid = entry.getKey();
            List<PayTb> payTbList = entry.getValue();
            boolean flag=true;
            buyCount.incrementAndGet();
            if(payTbList.size()>1){
                boolean reby=true;
                if(payTbList.size()==2){
                    for(PayTb payTb:payTbList){
                        if(payTb.getAmount()<2000){
                            reby=false;
                        }
                    }
                }
                if(reby){
                    manyBuyCount.incrementAndGet();
                }
            }
            for(PayTb payTb:payTbList){
                if(payTb.getAmount()>3800){
                    if(payTb.getAmount()==9900||payTb.getAmount()==17800||payTb.getAmount()==29800){
                    }else{
                        QueryWrapper<CommodityOrdersTb>commodityOrdersTbQueryWrapper =new QueryWrapper<>();
                        commodityOrdersTbQueryWrapper.eq("pay_sn",payTb.getPaySn());
                        commodityOrdersTbQueryWrapper.eq("site_id",payTb.getSiteId());
                        List<CommodityOrdersTb> commodityOrdersTbs = commodityOrdersTbMapper.selectList(commodityOrdersTbQueryWrapper);
                        CommodityOrdersTb commodityOrdersTb = commodityOrdersTbs.get(0);

                        CommodityTb commodityTb=commodityTbMapper.selectById(commodityOrdersTb.getCid());
                        if(commodityTb.getName().contains("洗车卡")){
                            flag=false;
                            payTb.setFlag(111);
                            if(payTb.getStatus()==2){
                                refundCount.incrementAndGet();
                                refundAmount.addAndGet(payTb.getRefund()/100);
                            }
                        }
                    }
                }
            }
            if(flag){
                iterator.remove();
            }
        }

        //所有买过chonzhigka的
        AtomicInteger re=new AtomicInteger(0);
        AtomicInteger unre=new AtomicInteger(0);

        AtomicInteger reamount=new AtomicInteger(0);
        for(Map.Entry<Integer,List<PayTb>> entry:payTbMap.entrySet()){
            Integer uid = entry.getKey();
            List<PayTb> payTbList = entry.getValue();
            payTbList.sort(Comparator.comparing(PayTb::getCreatedAt));
            long createdAt = 0;
            for(PayTb payTb:payTbList){
                if(payTb.getFlag()==111){
                    createdAt=payTb.getCreatedAt();
                }
            }
            long finalCreatedAt = createdAt;
            List<PayTb> after=payTbList.stream().filter(payTb -> payTb.getCreatedAt() > finalCreatedAt).collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(after)){
                re.incrementAndGet();
                reamount.addAndGet(after.stream().mapToInt(PayTb::getAmount).sum());
            }else{
                unre.incrementAndGet();
            }
        }

        System.out.println(buyCount.get()+"-"+manyBuyCount.get()+"-"+re.get()+"-"+unre.get()+"-"+reamount.get());
        return ResponseEntity.ok("");
    }
}
