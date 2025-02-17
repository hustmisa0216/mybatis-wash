package com.wash.entity;

import com.wash.entity.statistics.DailyPaperTb;
import com.wash.entity.statistics.FaSettlementTb;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 历史某一天的
 * @version 1.0.0
 * @date 2025/2/14
 * @Description
 */
@Data
@AllArgsConstructor
public class DailyData {

    private DailyPaperTb dailyPaperTb;
    private FaSettlementTb faSettlementTb;

}
