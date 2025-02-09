package com.wash.entity.franchisee;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class FranchiseeSiteTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer ownId;
    private Integer parentId;
    private Integer siteId;
    private BigDecimal ownPercent;
    private BigDecimal parentPercent;
    private Integer guaranteedAmount;
    private Integer settledAmount;
    private Byte operationType;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
}