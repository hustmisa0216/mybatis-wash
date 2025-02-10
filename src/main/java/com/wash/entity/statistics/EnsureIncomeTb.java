package com.wash.entity.statistics;

import lombok.Data;

import java.io.Serializable;

@Data
public class EnsureIncomeTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer date; // 年月日
    private Integer cityId; // 城市ID
    private Integer siteId; // 场地ID
    private Integer vipMoney; // VIP第三方消耗
    private Integer vipAmount; // VIP充值消耗
    private Integer vipCashback; // VIP返现消耗
    private Integer vipCard; // VIP转卡消耗
    private Integer vipDeposit; // VIP储值卡消耗
    private Integer washMoney; // 单次洗车第三方消耗
    private Integer washAmount; // 单次洗车充值消耗
    private Integer washCashback; // 单次洗车返现消耗
    private Integer washCard; // 单次洗车转卡消耗
    private Integer washDeposit; // 单次洗车储值卡消耗
    private Integer perMoney; // 次卡第三方消耗
    private Integer perAmount; // 次卡充值消耗
    private Integer perCashback; // 次卡返现消耗
    private Integer perCard; // 次卡转卡消耗
    private Integer perDeposit; // 次卡储值卡消耗
    private Integer washerFluidMoney; // 玻璃水第三方消耗
    private Integer washerFluidAmount; // 玻璃水充值消耗
    private Integer washerFluidCashback; // 玻璃水返现消耗
    private Integer washerFluidCard; // 玻璃水转卡消耗
    private Integer washerFluidDeposit; // 玻璃水储值卡消耗
    private Integer timingMoney; // 计时第三方消耗
    private Integer timingAmount; // 计时充值消耗
    private Integer timingCashback; // 计时返现消耗
    private Integer timingCard; // 计时转卡消耗
    private Integer timingDeposit; // 计时储值卡消耗
    private Integer prepaidMoney; // 洗车卡第三方消耗
    private Integer couponMoney; // 洗车券第三方消耗

}