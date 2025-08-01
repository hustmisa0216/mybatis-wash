package com.wash.entity.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wash.entity.BaseEntity;
import lombok.Data;

@Data
public class PayTb   extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Integer id; // 用户ID

    private Integer uid; // 用户ID
    private String paySn; // 订单号
    private String agreementSn; // 周期扣款订单号
    private Integer siteId; // 场地ID
    private Integer amount; // 支付金额
    private Integer refund; // 退款金额 全退完status才是已退款
    private Byte payType; // 支付类型
    private Byte payFor; // 支付原因
    private Integer activityId; // 充值活动ID
    private String ipAddr; // 用户IP
    private Integer isSandbox; // 是否沙盒
    private String transactionId; // 第三方Id
    private Long callbackAt; // 回调时间
    private Byte status; // 状态
    private Long createdAt; // 创建时间
    private Long updatedAt; // 更新时间
    private String ext; // 微信PrepayId
    private Long deletedAt; // 删除时间
    private Byte payFrom; // 支付来源页
    private String mchId; // 商户号
    private String attach; // 附加信息
    private String payment; // 支付凭据
    private Integer flag; // 标示

    @TableField(exist = false) // 标记此字段不参与数据库操作
    private int date;
    @TableField(exist = false) // 标记此字段不参与数据库操作
    private int dateMonth;


    public String toString(){
        return String.join(",",
                String.valueOf(id),
                String.valueOf(uid),
                paySn != null ? paySn : "",
                agreementSn != null ? agreementSn : "",
                String.valueOf(siteId),
                String.valueOf(amount),
                String.valueOf(refund),
                String.valueOf(payType),
                String.valueOf(payFor),
                String.valueOf(activityId),
                ipAddr != null ? ipAddr : "",
                String.valueOf(isSandbox),
                transactionId != null ? transactionId : "",
                String.valueOf(callbackAt),
                String.valueOf(status),
                String.valueOf(createdAt),
                String.valueOf(updatedAt),
                ext != null ? ext : "",
                deletedAt!= null? String.valueOf(deletedAt) : "",
                String.valueOf(payFrom),
                mchId != null ? mchId : "",
                attach != null ? attach : "",
                payment != null ? payment : "",
                String.valueOf(flag)
        );
    }
    public static PayTb fromString(String str) {
        PayTb payTb = new PayTb();
        String[] parts = str.split(",");
        if (parts.length < 24) {
            throw new IllegalArgumentException("输入字符串格式不正确");
        }

        // 解析 id
        if (!parts[0].isEmpty()) {
            payTb.setId(Integer.valueOf(parts[0]));
        }
        // 解析 uid
        if (!parts[1].isEmpty()) {
            payTb.setUid(Integer.valueOf(parts[1]));
        }
        // 解析 paySn
        payTb.setPaySn(parts[2].isEmpty() ? null : parts[2]);
        // 解析 agreementSn
        payTb.setAgreementSn(parts[3].isEmpty() ? null : parts[3]);
        // 解析 siteId
        if (!parts[4].isEmpty()) {
            payTb.setSiteId(Integer.valueOf(parts[4]));
        }
        // 解析 amount
        if (!parts[5].isEmpty()) {
            payTb.setAmount(Integer.valueOf(parts[5]));
        }
        // 解析 refund
        if (!parts[6].isEmpty()) {
            payTb.setRefund(Integer.valueOf(parts[6]));
        }
        // 解析 payType
        if (!parts[7].isEmpty()) {
            payTb.setPayType(Byte.valueOf(parts[7]));
        }
        // 解析 payFor
        if (!parts[8].isEmpty()) {
            payTb.setPayFor(Byte.valueOf(parts[8]));
        }
        // 解析 activityId
        if (!parts[9].isEmpty()) {
            payTb.setActivityId(Integer.valueOf(parts[9]));
        }
        // 解析 ipAddr
        payTb.setIpAddr(parts[10].isEmpty() ? null : parts[10]);
        // 解析 isSandbox
        if (!parts[11].isEmpty()) {
            payTb.setIsSandbox(Integer.valueOf(parts[11]));
        }
        // 解析 transactionId
        payTb.setTransactionId(parts[12].isEmpty() ? null : parts[12]);
        // 解析 callbackAt
        if (!parts[13].isEmpty()) {
            payTb.setCallbackAt(Long.valueOf(parts[13]));
        }
        // 解析 status
        if (!parts[14].isEmpty()) {
            payTb.setStatus(Byte.valueOf(parts[14]));
        }
        // 解析 createdAt
        if (!parts[15].isEmpty()) {
            payTb.setCreatedAt(Long.valueOf(parts[15]));
        }
        // 解析 updatedAt
        if (!parts[16].isEmpty()) {
            payTb.setUpdatedAt(Long.valueOf(parts[16]));
        }
        // 解析 ext
        payTb.setExt(parts[17].isEmpty() ? null : parts[17]);
        // 解析 deletedAt
        if (!parts[18].isEmpty()&&!"null".equals(parts[18])) {
            payTb.setDeletedAt(Long.valueOf(parts[18]));
        }
        // 解析 payFrom
        if (!parts[19].isEmpty()) {
            payTb.setPayFrom(Byte.valueOf(parts[19]));
        }
        // 解析 mchId
        payTb.setMchId(parts[20].isEmpty() ? null : parts[20]);
        // 解析 attach
        payTb.setAttach(parts[21].isEmpty() ? null : parts[21]);

        String payment="";
        if(str.contains("{")&&str.contains("}")) {
             payment = str.substring(str.indexOf("{"), str.indexOf("}")+1);
        }
        // 解析 payment
        payTb.setPayment(payment);
        // 解析 flag
        String flag="";
        if(str.contains("}")) {
            flag = str.substring(str.indexOf("}") + 2);
        }
        if (!flag.isEmpty()) {
            payTb.setFlag(Integer.valueOf(flag));
        }

        return payTb;
    }

}
