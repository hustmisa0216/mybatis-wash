package com.wash.entity.statistics;

public class SiteStatisticsTb {

    private int siteId;                      // 场地ID
    private int cityId;                      // 城市ID
    private String siteName;                 // 场地名称
    private String cityName;                 // 城市名称
    private int personCount;                 // 小区人数
    private int carCount;                    // 小区车辆数
    private byte machineCount;               // 机器数量 (tinyint)
    private int registerUser;                // 注册用户数
    private int activityUserCount;           // 活跃用户数
    private double activityUserRate;         // 活跃用户比例
    private int washUserCount;               // 洗车用户数
    private double washCoverageRate;         // 洗车覆盖率
    private int rechargeUserCount;           // 充值用户数
    private double rechargeCoverageRate;     // 充值覆盖率
    private double rechargeRate;             // 充值转化率
    private int totalTransaction;             // 总流水
    private int vendorTotalTransaction;      // 总流水-商户 对应场地财务和场地数据中的总流水
    private int totalConsume;                // 总消费
    private int dayAverageConsume;           // 日均消费
    private int totalRecharge;               // 总充值
    private int directRecharge;              // 后台直冲
    private int cardRecharge;                // 转卡
    private int dayAverageRecharge;          // 日均充值
    private int personAverageRecharge;       // 人均充值
    private int refundAmount;                // 退款总金额
    private int vendorRefundAmount;          // 退款总金额-商户
    private int washCarCount;                // 洗车次数        对应场地数据洗车次数
    private double personAverageWashCount;   // 人均洗车次数
    private double dayAverageWashCount;      // 日均洗车次数
    private int washDuration;                // 总洗车时长
    private int personAverageWashDuration;   // 人均洗车时长
    private int personAverageConsume;        // 人均消费
    private int operationDuration;           // 运营天数
    private int totalCallBack;               // 充值返现
    private int rechargeBalance;             // 用户充值余额
    private int cashbackBalance;             // 用户返现余额
    private int directBalance;               // 直充余额
    private int cardBalance;                 // 卡余额
    private byte doubleMachineCount;         // 双机版的机器数量
    private int totalBalance;                // 总余额
    private int vipUserCount;                // 当前VIP用户数
    private int totalVipCount;               // 总VIP用户数
    private int continueVipCount;            // 续费过VIP的用户数
    private int yearVipCommodityCount;       // 年卡商品销售数
    private int arpu;                        // ARPU
    private int vendorNoSharingAmount;       // 不分账金额-商户
    private int vendorUnsettledAmount;       // 未结算金额-商户
    private Integer vendorSettledExpiredAmount; // 过期结算金额-商户
    private int faTotalTransaction;          // 加盟商总流水

    // Getter 和 Setter 方法
    // 省略了具体的实现，通常使用 IDE 自动生成

    // toString 方法
    @Override
    public String toString() {
        return "SiteStatistics{" +
                "siteId=" + siteId +
                ", cityId=" + cityId +
                ", siteName='" + siteName + '\'' +
                ", cityName='" + cityName + '\'' +
                ", personCount=" + personCount +
                ", carCount=" + carCount +
                ", machineCount=" + machineCount +
                ", registerUser=" + registerUser +
                ", activityUserCount=" + activityUserCount +
                ", activityUserRate=" + activityUserRate +
                ", washUserCount=" + washUserCount +
                ", washCoverageRate=" + washCoverageRate +
                ", rechargeUserCount=" + rechargeUserCount +
                ", rechargeCoverageRate=" + rechargeCoverageRate +
                ", rechargeRate=" + rechargeRate +
                ", totalTransaction=" + totalTransaction +
                ", vendorTotalTransaction=" + vendorTotalTransaction +
                ", totalConsume=" + totalConsume +
                ", dayAverageConsume=" + dayAverageConsume +
                ", totalRecharge=" + totalRecharge +
                ", directRecharge=" + directRecharge +
                ", cardRecharge=" + cardRecharge +
                ", dayAverageRecharge=" + dayAverageRecharge +
                ", personAverageRecharge=" + personAverageRecharge +
                ", refundAmount=" + refundAmount +
                ", vendorRefundAmount=" + vendorRefundAmount +
                ", washCarCount=" + washCarCount +
                ", personAverageWashCount=" + personAverageWashCount +
                ", dayAverageWashCount=" + dayAverageWashCount +
                ", washDuration=" + washDuration +
                ", personAverageWashDuration=" + personAverageWashDuration +
                ", personAverageConsume=" + personAverageConsume +
                ", operationDuration=" + operationDuration +
                ", totalCallBack=" + totalCallBack +
                ", rechargeBalance=" + rechargeBalance +
                ", cashbackBalance=" + cashbackBalance +
                ", directBalance=" + directBalance +
                ", cardBalance=" + cardBalance +
                ", doubleMachineCount=" + doubleMachineCount +
                ", totalBalance=" + totalBalance +
                ", vipUserCount=" + vipUserCount +
                ", totalVipCount=" + totalVipCount +
                ", continueVipCount=" + continueVipCount +
                ", yearVipCommodityCount=" + yearVipCommodityCount +
                ", arpu=" + arpu +
                ", vendorNoSharingAmount=" + vendorNoSharingAmount +
                ", vendorUnsettledAmount=" + vendorUnsettledAmount +
                ", vendorSettledExpiredAmount=" + vendorSettledExpiredAmount +
                ", faTotalTransaction=" + faTotalTransaction +
                '}';
    }
}
