package com.wash.entity.data;

import com.baomidou.mybatisplus.annotation.TableField;
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

    @TableField(exist = false) // 标记此字段不参与数据库操作
    private String date;
    @TableField(exist = false) // 标记此字段不参与数据库操作
    private String dateMonth;

    @Override
    public String toString() {
        return String.join(",",
                String.valueOf(id),
                String.valueOf(vendorId),
                String.valueOf(siteId),
                String.valueOf(amount),
                String.valueOf(type),
                String.valueOf(subType),
                transactionId != null ? transactionId : "",
                String.valueOf(flag),
                String.valueOf(createdAt)
        );
    }
}
