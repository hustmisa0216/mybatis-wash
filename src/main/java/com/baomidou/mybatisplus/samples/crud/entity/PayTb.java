package com.baomidou.mybatisplus.samples.crud.entity;

import lombok.Data;

@Data
public class PayTb {
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
    private Boolean isSandbox; // 是否沙盒
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
}
