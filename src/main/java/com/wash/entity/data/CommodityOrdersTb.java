package com.wash.entity.data;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
@Data
public class CommodityOrdersTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String orderId;
    private String depositOrderId;
    private Integer uid;
    private Integer cid;
    private Integer siteId;
    private String paySn;
    private Byte status;
    private String expressNum;
    private Long expressdAt;
    private Integer revUid;
    private String revName;
    private String phone;
    private String addr;
    private Integer paymentType;
    private String ext;
    private String voucherId;
    private Integer paymentDeposit;
    private Integer paymentMoney;
    private Integer paymentBalance;
    private Integer paymentTotal;
    private Integer priceTotal;
    private Integer refundAmount;
    private Long createdAt;
    private Long deletedAt;
    private Long updatedAt;
    private Integer vipDuration;
    private Long vipExpiredAt;
    private Integer vipQuota;
    private Byte deliveryStatus;
    private Integer perUseCard;
    private Integer perUseCardDuration;
    private Long perUseCardExpiredAt;
    private Integer washerFluid;
    private Integer timing;
    private Integer timingDuration;
    private Long timingExpiredAt;
    private Integer prepaidDuration;
    private Long prepaidExpiredAt;
    private Integer couponRule;
    private Integer couponDuration;
    private Integer usedAmount;
    private Integer usedQuota;
    private Integer skuId;
    private Integer count;
    private Integer parentId;
    private Integer suitId;
    private String remark;
    private Integer expressFee;
    private Integer freightCost;
    private String sets;
    private Integer flag;

    @TableField(exist = false) // 标记此字段不参与数据库操作
    private String date;
    @TableField(exist = false) // 标记此字段不参与数据库操作
    private String dateMonth;

    @Override
    public String toString() {
        return String.join(",",
                String.valueOf(id),
                orderId != null ? orderId : "",
                depositOrderId != null ? depositOrderId : "",
                String.valueOf(uid),
                String.valueOf(cid),
                String.valueOf(siteId),
                paySn != null ? paySn : "",
                String.valueOf(status),
                expressNum != null ? expressNum : "",
                String.valueOf(expressdAt),
                String.valueOf(revUid),
                revName != null ? revName : "",
                phone != null ? phone : "",
                addr != null ? addr : "",
                String.valueOf(paymentType),
                ext != null ? ext : "",
                voucherId != null ? voucherId : "",
                String.valueOf(paymentDeposit),
                String.valueOf(paymentMoney),
                String.valueOf(paymentBalance),
                String.valueOf(paymentTotal),
                String.valueOf(priceTotal),
                String.valueOf(refundAmount),
                String.valueOf(createdAt),
                String.valueOf(deletedAt),
                String.valueOf(updatedAt),
                String.valueOf(vipDuration),
                String.valueOf(vipExpiredAt),
                String.valueOf(vipQuota),
                String.valueOf(deliveryStatus),
                String.valueOf(perUseCard),
                String.valueOf(perUseCardDuration),
                String.valueOf(perUseCardExpiredAt),
                String.valueOf(washerFluid),
                String.valueOf(timing),
                String.valueOf(timingDuration),
                String.valueOf(timingExpiredAt),
                String.valueOf(prepaidDuration),
                String.valueOf(prepaidExpiredAt),
                String.valueOf(couponRule),
                String.valueOf(couponDuration),
                String.valueOf(usedAmount),
                String.valueOf(usedQuota),
                String.valueOf(skuId),
                String.valueOf(count),
                String.valueOf(parentId),
                String.valueOf(suitId),
                remark != null ? remark : "",
                String.valueOf(expressFee),
                String.valueOf(freightCost),
                sets != null ? sets : "",
                String.valueOf(flag)
        );
    }
}