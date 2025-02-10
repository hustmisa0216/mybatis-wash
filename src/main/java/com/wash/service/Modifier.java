package com.wash.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.entity.ModifierData;
import com.wash.entity.Series;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.franchisee.FranchiseeTb;
import com.wash.entity.statistics.DailyPaperTb;
import com.wash.entity.statistics.EnsureIncomeTb;
import com.wash.entity.statistics.FaSettlementTb;
import com.wash.entity.statistics.MonthPaperTb;
import com.wash.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Modifier {

    @Autowired
    private DailyPaperTbMapper dailyPaperTbMapper;
    @Autowired
    private FaSettlementTbMapper faSettlementTbMapper;
    @Autowired
    private EnsureIncomeTbMapper ensureIncomeTbMapper;
    @Autowired
    private MonthPaperTbMapper monthPaperTbMapper;
    @Autowired
    private FranchiseeTbMapper franchiseeTbMapper;

    public void delete(String vendorId, FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, List<Series> seriesList) {

        for(Series series:seriesList){

        }

    }
    public void update(String vendorId, FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, List<Series> resSeries) {

        ModifierData modifierData=new ModifierData(resSeries);
        UpdateWrapper<DailyPaperTb> dailyPaperTbUpdateWrapper=new UpdateWrapper<>();

        for(String date:modifierData.getDAY_CHARGEAMOUNT_MAP().keySet()){
        }





    }
}
