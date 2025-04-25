package com.wash.entity.u;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/4/25
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTb {

    private Integer id;
    private String phone;
    private Byte type;
    private String wechatAppOpenid;
    private String wechatMiniOpenid;
    private String wechatUnionid;
    private String alipayOpenid;
    private String sessionId;
    private String avatar;
    private Integer rechargeBalance;
    private Integer cashbackBalance;
    private Integer directBalance;
    private Integer cardBalance;
    private Integer depositBalance;
    private String registrationId;
    private Byte loginType;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
    private String nickname;
    private Integer promoter;
    private Byte promoterType;
    private Integer channelId;
    private Integer siteId;
    private Integer machineId;
    private Integer activeness;
    private Boolean isVip;
    private Long vipExpiredAt;
    private Integer vipQuota;
    private String commodityIds;
    private Long vipFreezeAt;
    private Long vipFreezeEndAt;
    private Integer perUseCard;
    private Long perUseCardExpiredAt;
    private Long perUseCardFreezeAt;
    private Long vipBlockAt;
    private Long vipBlockEndAt;
    private Integer washerFluid;
    private Integer timing;
    private Long timingExpiredAt;
    private Long timingFreezeAt;
    private Integer gasolineBalance;
    private Integer prepaidBalance;
    private Long prepaidExpiredAt;
    private Long prepaidFreezeAt;
    private Long couponFreezeAt;

    @TableField(exist = false) // 标记此字段不参与数据库操作
    private int date;
    @TableField(exist = false) // 标记此字段不参与数据库操作
    private int dateMonth;
}
