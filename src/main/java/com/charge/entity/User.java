package com.charge.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class User {
    private int id;
    private String phone; // 手机号码
    @TableField(value= "wechat_openid")
    private String wechatOpenId; // 微信openid
    private String sessionId; // 会话ID
    private int balanceRecharge; // 充值余额
    private int activeness; // 活跃度
    private int siteId; // 场地ID
    private Integer createdAt; // 创建时间
    private Integer updatedAt; // 更新时间
}
