package com.wash.entity.data;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;

/**
 * 商品信息表实体类
 * 对应数据库表：commodity_tb
 */
@Data
public class CommodityTb implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（自增）
     */
    private Integer id;

    /**
     * 商品名
     */
    private String name;

    /**
     * 商品价值
     */
    private Integer price;

    /**
     * 周期扣款价格
     */
    private Integer agreementPrice = 0;

    /**
     * 周期扣款首次价格
     */
    private Integer agreementFirstPrice = 0;

    /**
     * 商品描述（注意：desc是MySQL关键字，需用反引号转义）
     */
    @TableField("`desc`") // 使用反引号
    private String desc;

    /**
     * 商品图URL
     */
    private String imageUrl;

    /**
     * 商品缩略图URL
     */
    private String thumbUrl;

    /**
     * 发货方式（0:红包 1:优惠券 2:快递）
     */
    private Integer deliveryMethod = 0;

    /**
     * 优惠券规则ID
     */
    private Integer voucherRuleId = 0;

    /**
     * 上架状态
     */
    private Integer status = 0;

    /**
     * 红包范围（在price基础上浮动: (price+red_packet_range) 到 (price - red_packet_range)）
     */
    private Integer redPacketRange = 0;

    /**
     * vip可洗车次数
     */
    private Integer vipWashingCount = 0;

    /**
     * vip时间（时长，单位需结合业务定义，如天/月）
     */
    private Integer vipDuration = 0;

    /**
     * vip限额
     */
    private Integer vipQuota = 0;

    /**
     * 玻璃水次数
     */
    private Integer washerFluid = 0;

    /**
     * 计时时间（单位需结合业务定义，如分钟）
     */
    private Integer timing = 0;

    /**
     * 洗车卡有效期（单位需结合业务定义，如天）
     */
    private Integer prepaidDuration = 0;

    /**
     * 添加时间（时间戳，毫秒）
     */
    private Long createdAt;

    /**
     * 删除时间（逻辑删除标记，时间戳，毫秒；null表示未删除）
     */
    private Long deletedAt;

    /**
     * 适用级别（0全国 1场地）
     */
    private Integer usedLevel = 0;

    /**
     * 原价
     */
    private Integer originalPrice = 0;

    /**
     * 折扣角标
     */
    private String discountBadge;

    /**
     * 优惠文案（暂首购才显示）
     */
    private String discountContent;

    /**
     * 排序（顺序，小到大）
     */
    private Integer sort = 0;

    /**
     * 折扣角标文案
     */
    private String discountBadgeContent;

    /**
     * 会员卡周期类型
     */
    private Integer durationType = 0;

    /**
     * 更新时间（时间戳，毫秒）
     */
    private Long updatedAt;

    /**
     * 商品属性（格式需结合业务定义，如JSON字符串/逗号分隔）
     */
    private String attrList;

    /**
     * 商品分类编号
     */
    private Integer categoryId = 0;

    /**
     * 详情（长文本）
     */
    private String detail;

    /**
     * 决招产品ID（英文逗号分隔）
     */
    private String crmObjectIds;
}
