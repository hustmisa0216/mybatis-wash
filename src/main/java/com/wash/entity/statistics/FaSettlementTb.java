package com.wash.entity.statistics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class FaSettlementTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer ownId;
    private Integer date; // yyyyMMdd 格式
    private Integer siteId;
    private Byte type; // 1-加盟结算 2-代理结算
    private BigDecimal rate;
    private Integer earnings; // 分成金额分
    private Integer guarantee; // 保底金额
    private Long createdAt; // 创建时间
}