package com.wash.entity.data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wash.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
@Data
public class CommodityOrdersTb  extends BaseEntity implements Serializable  {
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
    private int date;
    @TableField(exist = false) // 标记此字段不参与数据库操作
    private int dateMonth;

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
                deletedAt!= null? String.valueOf(deletedAt) : "",
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

    public static CommodityOrdersTb fromString(String str) {
        CommodityOrdersTb obj = new CommodityOrdersTb();
        String[] parts = str.split(",");
        if (parts.length < 52) {
            return obj;
        }

        try {
            obj.setId(Integer.valueOf(parts[0]));
            obj.setOrderId(parts[1].isEmpty() ? null : parts[1]);
            obj.setDepositOrderId(parts[2].isEmpty() ? null : parts[2]);
            obj.setUid(Integer.valueOf(parts[3]));
            obj.setCid(Integer.valueOf(parts[4]));
            obj.setSiteId(Integer.valueOf(parts[5]));
            obj.setPaySn(parts[6].isEmpty() ? null : parts[6]);
            obj.setStatus(Byte.valueOf(parts[7]));
            obj.setExpressNum(parts[8].isEmpty() ? null : parts[8]);
            obj.setExpressdAt(Long.valueOf(parts[9]));
            obj.setRevUid(Integer.valueOf(parts[10]));
            obj.setRevName(parts[11].isEmpty() ? null : parts[11]);
            obj.setPhone(parts[12].isEmpty() ? null : parts[12]);
            obj.setAddr(parts[13].isEmpty() ? null : parts[13]);
            obj.setPaymentType(Integer.valueOf(parts[14]));
            obj.setExt(parts[15].isEmpty() ? null : parts[15]);
            obj.setVoucherId(parts[16].isEmpty() ? null : parts[16]);
            obj.setPaymentDeposit(Integer.valueOf(parts[17]));
            obj.setPaymentMoney(Integer.valueOf(parts[18]));
            obj.setPaymentBalance(Integer.valueOf(parts[19]));
            obj.setPaymentTotal(Integer.valueOf(parts[20]));
            obj.setPriceTotal(Integer.valueOf(parts[21]));
            obj.setRefundAmount(Integer.valueOf(parts[22]));
            obj.setCreatedAt(Long.valueOf(parts[23]));
            obj.setDeletedAt(parts[24].isEmpty() ? null : Long.valueOf(parts[24]));
            obj.setUpdatedAt(Long.valueOf(parts[25]));
            obj.setVipDuration(Integer.valueOf(parts[26]));
            obj.setVipExpiredAt(Long.valueOf(parts[27]));
            obj.setVipQuota(Integer.valueOf(parts[28]));
            obj.setDeliveryStatus(Byte.valueOf(parts[29]));
            obj.setPerUseCard(Integer.valueOf(parts[30]));
            obj.setPerUseCardDuration(Integer.valueOf(parts[31]));
            obj.setPerUseCardExpiredAt(Long.valueOf(parts[32]));
            obj.setWasherFluid(Integer.valueOf(parts[33]));
            obj.setTiming(Integer.valueOf(parts[34]));
            obj.setTimingDuration(Integer.valueOf(parts[35]));
            obj.setTimingExpiredAt(Long.valueOf(parts[36]));
            obj.setPrepaidDuration(Integer.valueOf(parts[37]));
            obj.setPrepaidExpiredAt(Long.valueOf(parts[38]));
            obj.setCouponRule(Integer.valueOf(parts[39]));
            obj.setCouponDuration(Integer.valueOf(parts[40]));
            obj.setUsedAmount(Integer.valueOf(parts[41]));
            obj.setUsedQuota(Integer.valueOf(parts[42]));
            obj.setSkuId(Integer.valueOf(parts[43]));
            obj.setCount(Integer.valueOf(parts[44]));
            obj.setParentId(Integer.valueOf(parts[45]));
            obj.setSuitId(Integer.valueOf(parts[46]));
            obj.setRemark(parts[47].isEmpty() ? null : parts[47]);
            obj.setExpressFee(Integer.valueOf(parts[48]));
            obj.setFreightCost(Integer.valueOf(parts[49]));
            obj.setSets(parts[50].isEmpty() ? null : parts[50]);
            obj.setFlag(Integer.valueOf(parts[51]));
        } catch (NumberFormatException e) {
            // 处理数字转换异常
        }
        return obj;
    }
}