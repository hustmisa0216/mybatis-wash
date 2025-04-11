package com.wash.service.calculator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wash.entity.franchisee.FaWithdrawTb;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.entity.statistics.EnsureIncomeTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.mapper.EnsureIncomeTbMapper;
import com.wash.mapper.FaSettlementTbMapper;
import com.wash.mapper.FaWithdrawTbMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/4/10
 * @Description
 */
@Service
public class DrawCalculator {

    @Autowired
    private FaWithdrawTbMapper faWithdrawTbMapper;

    @Autowired
    private EnsureIncomeTbMapper ensureIncomeTbMapper;

    @Autowired
    private FaSettlementTbMapper faSettlementTbMapper;


    public LessReason drawCalculate(int inputVendorId, FranchiseeTb franchiseeTb,boolean major) {

        int majorNum=major?0:-80000;
        QueryWrapper<FaWithdrawTb> faWithdrawTbQueryWrapper = new QueryWrapper<>();
        faWithdrawTbQueryWrapper.eq("own_id", inputVendorId);
        List<FaWithdrawTb> faWithdrawTbs = faWithdrawTbMapper.selectList(faWithdrawTbQueryWrapper);
        faWithdrawTbs.sort((a,c)->(int)(c.getCreatedAt()-a.getCreatedAt()));

        FaWithdrawTb faWithdrawTb = faWithdrawTbs.get(0);// 取最近的一次提车
        long lastTime = faWithdrawTb.getCreatedAt();
        long lastUpdateTime = faWithdrawTb.getUpdatedAt();

        boolean eq=lastTime==lastUpdateTime;

        // 创建 Calendar 实例并设置时间
        Calendar calendar = Calendar.getInstance();
        int diff = (int) ((System.currentTimeMillis() / 1000 - lastTime) / ( 60 * 60));//小时数

        if (eq) {//融了
            if(diff<36&&franchiseeTb.getWaitWithdraw()<2300*100+majorNum){
                return new LessReason("刚融完且小于2300",true);
            }
        } else {//等待中
            if(diff<18&&franchiseeTb.getWaitWithdraw()<2700*100+majorNum){
                return new LessReason("等待中且小于2000",false);
            }
        }

        calendar.setTimeInMillis(lastTime*1000);

        // 获取当前日期
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // 获取当月最后一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // 使用 add 方法，将日期加一个月并减一天
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);//当月最后一天
        int lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if(currentDay>lastDayOfMonth-3&&diff<24*30){
           return new LessReason("月末3天以内",false);
        }

        QueryWrapper<FaSettlementTb> faSettlementTbQueryWrapper = new QueryWrapper<>();
        faSettlementTbQueryWrapper.eq("own_id", inputVendorId);
        faSettlementTbQueryWrapper.ge("created_at", lastTime);
        faSettlementTbQueryWrapper.select("SUM(earnings) as vipMoney");

        List<Map<String, Object>> resultList = faSettlementTbMapper.selectMaps(faSettlementTbQueryWrapper);
        if(resultList==null||resultList.size()==0){
            if(franchiseeTb.getWaitWithdraw()>5000*100+majorNum*2){
                return new LessReason("",true);
            }else{
                return new LessReason("暂无融记录",false);
            }
        }
        Map<String, Object> resultMap = resultList.get(0);

        if(resultList==null||resultList.size()==0){
            if(franchiseeTb.getWaitWithdraw()>7000*100+majorNum*2){
                return new LessReason("",true);
            }else{
                return new LessReason("暂无融记录",false);
            }
        }

        double vipM = resultMap.get("vipMoney") != null ? Double.parseDouble(resultMap.get("vipMoney").toString()) : 0;
        double rest=franchiseeTb.getWaitWithdraw()-vipM;//余下的车

        if(franchiseeTb.getWaitWithdraw()<2000*100&&rest<1350*100){
             return new LessReason("上次融少于1350",false);
        }
        return new LessReason("",true);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class LessReason{
        String reason;
        boolean isValid;
    }

}
