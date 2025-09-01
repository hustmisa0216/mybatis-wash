package com.wash.service.consume;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.entity.data.VendorProfitSharingTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.mapper.*;
import com.wash.service.calculator.VenCalculator;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/8/26
 * @Description
 */
@Service
public class ConsumeHandler {

    private VenCalculator venCalculator;

    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;
    @Autowired
    private PayTbMapper payTbMapper;
    @Autowired
    private VendorProfitSharingTbMapper vendorProfitSharingTbMapper;

    @Autowired
    private UserTbMapper userTbMapper;

    @Autowired
    private CommodityOrdersTbMapper commodityOrdersTbMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private CommodityOrderProfitSharingTbMapper commodityOrderProfitSharingTbMapper;

    @Autowired
    private DateGenerator dateGenerator;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private int siteids []={865, 1187, 738, 766, 818, 849};

    public void start(){
        long time = System.currentTimeMillis();
        String lastDay = SIMPLE_DATE_FORMAT.format(new Date(time));
        long zeroTime=time;
        try {
             zeroTime=SIMPLE_DATE_FORMAT.parse(lastDay).getTime()/1000;
        } catch (ParseException e) {

        }

        QueryWrapper<VendorProfitSharingTb> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("site_id",siteids);
        queryWrapper.ge("created_at",zeroTime);
        List<VendorProfitSharingTb> vendorProfitSharingTbs = vendorProfitSharingTbMapper.selectList(queryWrapper);
        Map<Integer, Map<Integer, List<VendorProfitSharingTb>>> vendorProfitSharingTbMap =
                vendorProfitSharingTbs.stream().collect(
                        Collectors.groupingBy(
                                VendorProfitSharingTb::getSiteId,
                                Collectors.groupingBy(
                                        VendorProfitSharingTb::getVendorId,  // 第二层key改为vendorId
                                        Collectors.toList())
                        )
                );


        for(int siteId:vendorProfitSharingTbMap.keySet()){
            Map<Integer, List<VendorProfitSharingTb>> vendorMap = vendorProfitSharingTbMap.get(siteId);

            for(int vendorId:vendorMap.keySet()){
                List<VendorProfitSharingTb> vvTbs = vendorMap.get(vendorId);
                if(CollectionUtils.isNotEmpty(vvTbs)){
                    List<VendorProfitSharingTb> positiveList = vvTbs.stream().filter(v -> v.getAmount() > 0).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(positiveList)) {
                        int amount = positiveList.stream().mapToInt(VendorProfitSharingTb::getAmount).sum();

                        int cut = amount * 3 / 4;
                        int sum = 0;
                        List<VendorProfitSharingTb> res = new ArrayList<>();
                        for (VendorProfitSharingTb v : positiveList) {
                            sum += v.getAmount();
                            if (sum >= cut) {
                                break;
                            }
                            res.add(v);
                        }

                       // vendorProfitSharingTbMapper.deleteBatchIds(res);
                        int vendorid = vvTbs.get(0).getVendorId();
                        UpdateWrapper<FranchiseeTb> franchiseeTbUpdateWrapper = new UpdateWrapper<>();
                        franchiseeTbUpdateWrapper.eq("id",vendorid);
                        franchiseeTbUpdateWrapper.setSql("settled_amount = settled_amount-" + sum)
                                .setSql("wait_withdraw = wait_withdraw-" + sum)
                                .setSql("stmt_recharge_amount = stmt_recharge_amount-" +sum)
                                .setSql("stmt_profit_amount = stmt_profit_amount-" + sum);

                        int update = franchiseeTbMapper.update(null, franchiseeTbUpdateWrapper);

                    }
                }
            }
        }


    }
}
