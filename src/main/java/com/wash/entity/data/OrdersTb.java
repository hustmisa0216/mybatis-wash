package com.wash.entity.data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wash.entity.BaseEntity;
import lombok.Data;

@Data
public class OrdersTb   extends BaseEntity {

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

    @TableField(exist = false) // 标记此字段不参与数据库操作
    private int date;
    @TableField(exist = false) // 标记此字段不参与数据库操作
    private int dateMonth;

    public static OrdersTb fromString(String str) {
        OrdersTb ordersTb = new OrdersTb();
        String[] parts = str.split(",");
        if (parts.length < 40) {
            throw new IllegalArgumentException("输入字符串格式不正确");
        }

        // 解析 id
        if (!parts[0].isEmpty()) {
            ordersTb.setId(Integer.valueOf(parts[0]));
        }
        // 解析 orderId
        ordersTb.setOrderId(parts[1].isEmpty() ? null : parts[1]);
        // 解析 type
        if (!parts[2].isEmpty()) {
            ordersTb.setType(Byte.valueOf(parts[2]));
        }
        // 解析 siteId
        if (!parts[3].isEmpty()) {
            ordersTb.setSiteId(Integer.valueOf(parts[3]));
        }
        // 解析 machineId
        if (!parts[4].isEmpty()) {
            ordersTb.setMachineId(Integer.valueOf(parts[4]));
        }
        // 解析 uid
        if (!parts[5].isEmpty()) {
            ordersTb.setUid(Integer.valueOf(parts[5]));
        }
        // 解析 plateNumber
        ordersTb.setPlateNumber(parts[6].isEmpty() ? null : parts[6]);
        // 解析 userPhone
        ordersTb.setUserPhone(parts[7].isEmpty() ? null : parts[7]);
        // 解析 agencyUid
        if (!parts[8].isEmpty()) {
            ordersTb.setAgencyUid(Integer.valueOf(parts[8]));
        }
        // 解析 comboId
        if (!parts[9].isEmpty()) {
            ordersTb.setComboId(Integer.valueOf(parts[9]));
        }
        // 解析 comboType
        if (!parts[10].isEmpty()) {
            ordersTb.setComboType(Byte.valueOf(parts[10]));
        }
        // 解析 comboPrice
        if (!parts[11].isEmpty()) {
            ordersTb.setComboPrice(Integer.valueOf(parts[11]));
        }
        // 解析 comboTime
        if (!parts[12].isEmpty()) {
            ordersTb.setComboTime(Integer.valueOf(parts[12]));
        }
        // 解析 paymentType
        if (!parts[13].isEmpty()) {
            ordersTb.setPaymentType(Byte.valueOf(parts[13]));
        }
        // 解析 voucherId
        ordersTb.setVoucherId(parts[14].isEmpty() ? null : parts[14]);
        // 解析 paySn
        ordersTb.setPaySn(parts[15].isEmpty() ? null : parts[15]);
        // 解析 thirdPartyPayment
        if (!parts[16].isEmpty()&&!parts[16].equals("null")) {
            ordersTb.setThirdPartyPayment(Byte.valueOf(parts[16]));
        }
        // 解析 paymentMoney
        if (!parts[17].isEmpty()) {
            ordersTb.setPaymentMoney(Integer.valueOf(parts[17]));
        }
        // 解析 paymentBalance
        if (!parts[18].isEmpty()) {
            ordersTb.setPaymentBalance(Integer.valueOf(parts[18]));
        }
        // 解析 paymentPrepaid
        if (!parts[19].isEmpty()) {
            ordersTb.setPaymentPrepaid(Integer.valueOf(parts[19]));
        }
        // 解析 paymentTotal
        if (!parts[20].isEmpty()) {
            ordersTb.setPaymentTotal(Integer.valueOf(parts[20]));
        }
        // 解析 agencyProfit
        if (!parts[21].isEmpty()) {
            ordersTb.setAgencyProfit(Byte.valueOf(parts[21]));
        }
        // 解析 status
        if (!parts[22].isEmpty()) {
            ordersTb.setStatus(Byte.valueOf(parts[22]));
        }
        // 解析 washingStatus
        if (!parts[23].isEmpty()) {
            ordersTb.setWashingStatus(Integer.valueOf(parts[23]));
        }
        // 解析 ext
        ordersTb.setExt(parts[24].isEmpty() ? null : parts[24]);
        // 解析 startAt
        if (!parts[25].isEmpty()) {
            ordersTb.setStartAt(Long.valueOf(parts[25]));
        }
        // 解析 endAt
        if (!parts[26].isEmpty()) {
            ordersTb.setEndAt(Long.valueOf(parts[26]));
        }
        // 解析 createdAt
        if (!parts[27].isEmpty()) {
            ordersTb.setCreatedAt(Long.valueOf(parts[27]));
        }
        // 解析 updatedAt
        if (!parts[28].isEmpty()) {
            ordersTb.setUpdatedAt(Long.valueOf(parts[28]));
        }
        // 解析 deletedAt
        if (!parts[29].isEmpty()&&!parts[29].equals("null")) {
            ordersTb.setDeletedAt(Long.valueOf(parts[29]));
        }
        // 解析 statisticsFlag
        if (!parts[30].isEmpty()) {
            ordersTb.setStatisticsFlag(Byte.valueOf(parts[30]));
        }
        // 解析 effectedAt
        if (!parts[31].isEmpty()) {
            ordersTb.setEffectedAt(Long.valueOf(parts[31]));
        }
        // 解析 commodityOrderId
        ordersTb.setCommodityOrderId(parts[32].isEmpty() ? null : parts[32]);
        // 解析 paidCancelAt
        if (!parts[33].isEmpty()) {
            ordersTb.setPaidCancelAt(Long.valueOf(parts[33]));
        }
        // 解析 lat
        if (!parts[34].isEmpty()) {
            ordersTb.setLat(Double.valueOf(parts[34]));
        }
        // 解析 lng
        if (!parts[35].isEmpty()) {
            ordersTb.setLng(Double.valueOf(parts[35]));
        }
        // 解析 distance
        if (!parts[36].isEmpty()) {
            ordersTb.setDistance(Integer.valueOf(parts[36]));
        }
        // 解析 os
        if (!parts[37].isEmpty()) {
            ordersTb.setOs(Byte.valueOf(parts[37]));
        }
        // 解析 statisticsAmount
        if (!parts[38].isEmpty()) {
            ordersTb.setStatisticsAmount(Integer.valueOf(parts[38]));
        }
        // 解析 flag
        if (!parts[39].isEmpty()) {
            ordersTb.setFlag(Integer.valueOf(parts[39]));
        }

        return ordersTb;
    }

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
                thirdPartyPayment!= null? thirdPartyPayment+"" : "",
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