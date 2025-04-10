package com.wash.service.calculator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wash.entity.franchisee.FaWithdrawTb;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.mapper.FaWithdrawTbMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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


    private void drawCalculate(int inputVendorId, List<FranchiseeSiteTb> franchiseeSiteTbs) {
        QueryWrapper<FaWithdrawTb> faWithdrawTbQueryWrapper = new QueryWrapper<>();
        faWithdrawTbQueryWrapper.eq("vendor_id", inputVendorId);
        faWithdrawTbQueryWrapper.eq("deleted_at", null);
        List<FaWithdrawTb> faWithdrawTbs = faWithdrawTbMapper.selectList(faWithdrawTbQueryWrapper);
        faWithdrawTbs.sort((a,c)->(int)(c.getCreatedAt()-a.getCreatedAt()));
    }

}
