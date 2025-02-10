package com.wash.entity.data;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommodityOrderProfitSharingTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String transactionId;
    private Integer siteId;
    private String orderId;
    private Integer deliveryMethod;
    private Integer rechargeAmount;
    private Integer cashbackAmount;
    private Integer directAmount;
    private Integer cardAmount;
    private Integer depositAmount;
    private Byte type;
    private Byte subType;
    private String relatedId;
    private Integer flag;
    private String vendor;
    @TableField("`desc`") // 使用反引号
    private String desc;
    private Long createdAt;

    @TableField(exist = false) // 标记此字段不参与数据库操作
    private String date;
    @TableField(exist = false) // 标记此字段不参与数据库操作
    private String dateMonth;
}