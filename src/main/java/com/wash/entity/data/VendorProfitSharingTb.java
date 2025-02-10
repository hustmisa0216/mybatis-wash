package com.wash.entity.data;

import lombok.Data;

import java.io.Serializable;
@Data
public class VendorProfitSharingTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer vendorId;
    private Integer siteId;
    private Integer amount;
    private Byte type;
    private Byte subType;
    private String transactionId;
    private Integer flag;
    private Long createdAt;
}
