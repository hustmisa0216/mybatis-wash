package com.charge.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class Pay {
    private int id;
    private String tradeNo;
    private int uid;
    private Integer siteId;
    private int amount;
    private int refund;
    private byte type;
    private byte tradeType;
    @TableField(value = "`for`") // 使用反引号包裹列名
    private byte rea;
    private byte status;
    private String mchId;
    private String transactionId;
    private String payer;
    private Long callbackAt;
    private Long refundAt;
    private long createdAt;
    private long updatedAt;

    // Getters and Setters
}
