package com.wash.entity.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String orderId;
    private Byte type;
    private Integer siteId;
    private Integer machineId;
    private Integer uid;
    private String plateNumber;
    private String userPhone;
    private Integer agencyUid;
    private Integer comboId;
    private Byte comboType;
    private Integer comboPrice;
    private Integer comboTime;
    private Byte paymentType;
    private String voucherId;
    private String paySn;
    private Byte thirdPartyPayment;
    private Integer paymentMoney;
    private Integer paymentBalance;
    private Integer paymentPrepaid;
    private Integer paymentTotal;
    private Byte agencyProfit;
    private Byte status;
    private Integer washingStatus;
    private String ext;
    private Long startAt;
    private Long endAt;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
    private Byte statisticsFlag;
    private Long effectedAt;
    private String commodityOrderId;
    private Long paidCancelAt;
    private Double lat;
    private Double lng;
    private Integer distance;
    private Byte os;
    private Integer statisticsAmount;
    private Integer flag;

}