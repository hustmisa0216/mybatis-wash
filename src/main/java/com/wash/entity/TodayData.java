package com.wash.entity;


import com.wash.entity.statistics.SiteLatestDataTb;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.DoubleSummaryStatistics;

@Data
@AllArgsConstructor
public class TodayData {
    //private DoubleSummaryStatistics doubleSummaryStatistics;

    private int lastDayEar;
    private SiteLatestDataTb siteLatestDataTb;
}
