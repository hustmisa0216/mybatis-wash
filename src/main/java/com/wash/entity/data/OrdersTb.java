package com.wash.entity.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersTb {

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

    public String toString() {
        return String.join(",",
                String.valueOf(id),
                orderId != null ? orderId : "",
                String.valueOf(type),
                String.valueOf(siteId),
                String.valueOf(machineId),
                String.valueOf(uid),
                plateNumber != null ? plateNumber : "",
                userPhone != null ? userPhone : "",
                String.valueOf(agencyUid),
                String.valueOf(comboId),
                String.valueOf(comboType),
                String.valueOf(comboPrice),
                String.valueOf(comboTime),
                String.valueOf(paymentType),
                voucherId != null ? voucherId : "",
                paySn != null ? paySn : "",
                String.valueOf(thirdPartyPayment),
                String.valueOf(paymentMoney),
                String.valueOf(paymentBalance),
                String.valueOf(paymentPrepaid),
                String.valueOf(paymentTotal),
                String.valueOf(agencyProfit),
                String.valueOf(status),
                String.valueOf(washingStatus),
                ext != null ? ext : "",
                String.valueOf(startAt),
                String.valueOf(endAt),
                String.valueOf(createdAt),
                String.valueOf(updatedAt),
                String.valueOf(deletedAt),
                String.valueOf(statisticsFlag),
                String.valueOf(effectedAt),
                commodityOrderId != null ? commodityOrderId : "",
                String.valueOf(paidCancelAt),
                String.valueOf(lat),
                String.valueOf(lng),
                String.valueOf(distance),
                String.valueOf(os),
                String.valueOf(statisticsAmount),
                String.valueOf(flag)
        );
    }
}