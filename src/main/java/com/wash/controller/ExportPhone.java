package com.wash.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.charge.entity.Pay;
import com.wash.entity.data.PayTb;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.site.SiteTb;
import com.wash.entity.u.UserTb;
import com.wash.mapper.FranchiseeSiteTbMapper;
import com.wash.mapper.PayTbMapper;
import com.wash.mapper.SiteTbMapper;
import com.wash.mapper.UserTbMapper;
import com.wash.recover.ReByOrders;
import com.wash.service.Recorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/6/17
 * @Description
 */
@RestController
@RequestMapping("/export") // 设置基础路径
public class ExportPhone {


    @Autowired
    private PayTbMapper payTbMapper;

    @Autowired
    private FranchiseeSiteTbMapper franchiseeSiteTbMapper;

    @Autowired
    private SiteTbMapper siteTbMapper;

    @Autowired
    private ReByOrders reByOrders;

    @Autowired
    private UserTbMapper userTbMapper;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");


    @GetMapping("/export")
    public ResponseEntity<String> create(
            @RequestParam(value = "vendor", required = true) Integer vendor) throws IOException {

        QueryWrapper<FranchiseeSiteTb> franchiseeSiteTbQueryWrapper=new QueryWrapper<>();
        franchiseeSiteTbQueryWrapper.eq("own_id",vendor);
        List<FranchiseeSiteTb> franchiseeSiteTbs = franchiseeSiteTbMapper.selectList(franchiseeSiteTbQueryWrapper);

        List<Integer> siteids=franchiseeSiteTbs.stream().map(FranchiseeSiteTb::getSiteId).collect(Collectors.toList());

        List<SiteTb> list=siteTbMapper.selectBatchIds(siteids);
        Map<Integer,String> siteNameMap=list.stream().collect(Collectors.toMap(SiteTb::getId,SiteTb::getName));
        QueryWrapper<PayTb> payTbQueryWrapper=new QueryWrapper<>();
        payTbQueryWrapper.in("site_id",siteids);
        payTbQueryWrapper.ge("status",1);
        payTbQueryWrapper.le("created_at",1744796276);
        List<PayTb> payTbs=payTbMapper.selectList(payTbQueryWrapper);

        Map<Integer,Map<Integer,List<PayTb>>> site_uid_pay=payTbs.stream()
                .collect(Collectors.groupingBy(PayTb::getSiteId,Collectors.groupingBy(PayTb::getUid)));


        FileWriter fileWriter=new FileWriter("d:\\mogo\\wash\\2.txt");
        for (Map.Entry<Integer, Map<Integer, List<PayTb>>> entry : site_uid_pay.entrySet()) {
            int siteId = entry.getKey();
            Map<Integer, List<PayTb>> uid_pay = entry.getValue();
            for (Map.Entry<Integer, List<PayTb>> entry2 : uid_pay.entrySet()) {
                int uid = entry2.getKey();
                if (reByOrders.uidSet.contains(uid)) {
                    continue;
                }
                List<PayTb> userPayTb = entry2.getValue();
                UserTb userTb = userTbMapper.selectById(uid);
                userPayTb.sort(Comparator.comparing(PayTb::getCreatedAt));

                for (PayTb payTb : userPayTb) {
                    String date = SIMPLE_DATE_FORMAT.format(new Date(payTb.getCreatedAt() * 1000));
                    fileWriter.write(siteNameMap.get(payTb.getSiteId()) + "," + payTb.getUid() + "," + userTb.getPhone() + "," + payTb.getAmount() / 100 + "," + date + "\n");
                    fileWriter.flush();
                }
            }
        }
        return ResponseEntity.ok("");

    }

}
