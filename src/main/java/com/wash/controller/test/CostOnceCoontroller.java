package com.wash.controller.test;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wash.entity.Series;
import com.wash.entity.constants.DeliveryMethodType;
import com.wash.entity.data.*;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.entity.site.SiteTb;
import com.wash.mapper.*;
import com.wash.service.Collector;
import com.wash.service.date.DateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/7/7
 * @Description
 */
@RestController

public class CostOnceCoontroller {

    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;
    @Autowired
    private FranchiseeSiteTbMapper franchiseeSiteTbMapper;

    @Autowired
    private CommodityOrdersTbMapper commodityOrdersTbMapper;

    @Autowired
    private PayTbMapper payTbMapper;

    @Autowired
    private CommodityTbMapper commodityTbMapper;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private CommodityOrderProfitSharingTbMapper commodityOrderProfitSharingTbMapper;

    @Autowired
    private Collector collector;

    @Autowired
    private SiteTbMapper siteTbMapper;
    @Autowired
    private DateGenerator dateGenerator;
    @Autowired
    private MachineTbMapper machineTbMapper;



    @GetMapping("rebuy/costOnce")
    public ResponseEntity<String> dec(
            @RequestParam(value = "own",required = true) Integer own) throws ParseException, IOException, InterruptedException {

        BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter("D:\\mogo\\onceupdate.csv"));
        Map<Integer,Integer> siteCountMap=new HashMap<>();
        QueryWrapper<FranchiseeSiteTb> queryWrapper=new QueryWrapper<FranchiseeSiteTb>();
        queryWrapper.isNull("deleted_at");
        queryWrapper.ge("own_percent",10);
        List<FranchiseeSiteTb> franchiseeSiteTbs = franchiseeSiteTbMapper.selectList(queryWrapper);

        Map<Integer,List<CostEntity>> costEntityMap=new HashMap<>();
        for(FranchiseeSiteTb franchiseeSiteTb:franchiseeSiteTbs){
                FranchiseeTb franchiseeTb= franchiseeTbMapper.selectById(franchiseeSiteTb.getOwnId());

                SiteTb siteTb=siteTbMapper.selectById(franchiseeSiteTb.getSiteId());
                QueryWrapper<PayTb> payTbQueryWrapper=new QueryWrapper<PayTb>();
                payTbQueryWrapper.eq("site_id",franchiseeSiteTb.getSiteId());
                payTbQueryWrapper.ge("created_at",SIMPLE_DATE_FORMAT.parse("20240401").getTime()/1000);
                payTbQueryWrapper.le("created_at",SIMPLE_DATE_FORMAT.parse("20250401").getTime()/1000);
                payTbQueryWrapper.eq("status", 1);
                payTbQueryWrapper.ge("amount",2900);
                List<PayTb> payTbs=payTbMapper.selectList(payTbQueryWrapper);

            ExecutorService executorService = Executors.newCachedThreadPool();
            int batchSize = 20;
            for (int i = 0; i < payTbs.size(); i += batchSize) {
                int end = Math.min(i + batchSize, payTbs.size());
                List<PayTb> batch = payTbs.subList(i, end);
                // 提交任务到线程池
                CopyOnWriteArrayList<CostEntity> costEntities = new CopyOnWriteArrayList<>();
                CountDownLatch countDownLatch = new CountDownLatch(batch.size());
                executorService.submit(() -> {
                    for (PayTb payTb : batch) {
                        try {
                            CostEntity costEntity = extracted(bufferedWriter, siteCountMap, costEntityMap, franchiseeSiteTb, franchiseeTb, siteTb, payTb,countDownLatch);
                            if(costEntity!=null) {
                                costEntities.add(costEntity);
                            }
                        } catch (Exception e) {
                            countDownLatch.countDown();
                        }
                    }
                });
                countDownLatch.await();
                for(CostEntity costEntity:costEntities) {
                    bufferedWriter.write(costEntity.toString()+"\n");
                    bufferedWriter.flush();
                }
            }
        }

        return ResponseEntity.ok("");
    }

    private CostEntity extracted(BufferedWriter bufferedWriter, Map<Integer, Integer> siteCountMap, Map<Integer, List<CostEntity>> costEntityMap, FranchiseeSiteTb franchiseeSiteTb, FranchiseeTb franchiseeTb, SiteTb siteTb, PayTb payTb, CountDownLatch countDownLatch) throws IOException {
        Series series=new Series(payTb, franchiseeSiteTb);
        CommodityOrdersTb commodityOrderTb = collector.fillCO(series, payTb);
        if (commodityOrderTb == null) {
            countDownLatch.countDown();
            return null;
        }

        QueryWrapper<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbQueryWrapper = new QueryWrapper<>();
        commodityOrderProfitSharingTbQueryWrapper
                .eq("order_id", commodityOrderTb.getOrderId());
        List<CommodityOrderProfitSharingTb> commodityOrderProfitSharingTbs = commodityOrderProfitSharingTbMapper.selectList(commodityOrderProfitSharingTbQueryWrapper);
        if (commodityOrderProfitSharingTbs == null || commodityOrderProfitSharingTbs.size() == 0) {
            countDownLatch.countDown();
            return null;
        }
        commodityOrderProfitSharingTbs.stream().forEach(i -> dateGenerator.generateDate(i));
        DeliveryMethodType deliveryMethodType = DeliveryMethodType.from(commodityOrderProfitSharingTbs.get(0).getDeliveryMethod());

        //结算额度
        DoubleSummaryStatistics orderProfit = commodityOrderProfitSharingTbs.stream().collect(Collectors.summarizingDouble(CommodityOrderProfitSharingTb::getRechargeAmount));
        Long expireTime = commodityOrderTb.getVipExpiredAt() == 0 ? commodityOrderTb.getPrepaidExpiredAt() : commodityOrderTb.getVipExpiredAt();
        if (expireTime == 0) {
            expireTime = commodityOrderTb.getCreatedAt() + commodityOrderTb.getCouponDuration();
        }

        String commodityOrderId = null;
        if (deliveryMethodType == DeliveryMethodType.VIP_TIME) {
            commodityOrderId = commodityOrderTb.getOrderId();
        }
        //结算完毕
        if (orderProfit.getSum() == payTb.getAmount().intValue() || expireTime <= System.currentTimeMillis() / 1000) {
            List<OrdersTb> ordersTbs = collector.fillOrders(commodityOrderTb, commodityOrderProfitSharingTbs, deliveryMethodType,
                    expireTime, commodityOrderId);

            int machineCount=0;
            if(siteCountMap.containsKey(franchiseeSiteTb.getSiteId())){
                machineCount= siteCountMap.get(franchiseeSiteTb.getSiteId());
            }else{
                QueryWrapper<MachineTb> machineTbQueryWrapper=new QueryWrapper<MachineTb>();
                machineTbQueryWrapper.eq("site_id", franchiseeSiteTb.getSiteId());
                List<MachineTb> machineTbs=machineTbMapper.selectList(machineTbQueryWrapper);
                machineCount=machineTbs.size();
                siteCountMap.put(franchiseeSiteTb.getSiteId(),machineCount);
            }

            CostEntity costEntity=new CostEntity(franchiseeSiteTb.getSiteId(), payTb.getAmount(),
                    machineCount, franchiseeTb.getId(), franchiseeTb.getName(), siteTb.getName(),ordersTbs.size());
            countDownLatch.countDown();
            return costEntity;
        }
        countDownLatch.countDown();
        return null;
    }
}
