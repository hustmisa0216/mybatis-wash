package com.charge.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/4/18
 * @Description
 */
@Data
@AllArgsConstructor
public class CharTodayData {

    List<SiteLatestData> siteLatestDatas;

    List<VendorProfitSharing> vendorProfitSharings;

    public CharTodayData(List<SiteLatestData> siteLatestDatas, List<VendorProfitSharing> vendorProfitSharings) {
        this.siteLatestDatas = siteLatestDatas;
        this.vendorProfitSharings = vendorProfitSharings;
    }
    private int sumRe;
    private int sumInco;
}
