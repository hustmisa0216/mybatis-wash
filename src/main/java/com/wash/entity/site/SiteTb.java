package com.wash.entity.site;

import lombok.Data;

import java.io.Serializable;
@Data
public class SiteTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer cityId;
    private String address;
    private Byte status;
    private Byte progressId;
    private Integer corpId;
    private Byte consumptionGrade;
    private Integer payMode;
    private String payModeSort;
    private Integer vipUsageMode;
    private Long commodityEffectTime;
    private Byte buildingTypeId;
    private Byte envTypeId;
    private Double lat;
    private Double lng;
    private String geoCode;
    private Float agencyProp;
    private String selfCombos;
    private Integer agencyCombo;
    private Integer isOpenVip;
    private Integer entranceFee;
    private Byte washingMinuteStatus;
    private Integer washingStartingPrice;
    private Integer washingStartingMinute;
    private Integer washingMinutePrice;
    private Integer profitSharingRatio;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
    private Integer personNum;
    private Integer carNum;
    private Long beginAt;
    private String rechargeActivitys;
    private String washerFluidActivity;
    private String phone;
    private Long freezeAt;
    private Long freezeEndAt;
    private String freezeReason;
    private String agencyCombos;
    private String repairPhone;
    private String repairContact;
    private String images;
    private Integer washingDistanceLimit;
    private Byte maturity;
    private Byte showOnMap;
    private Long freeWashingBeginAt;
    private Long freeWashingEndAt;
    private Long regPresentBeginAt;
    private Long regPresentEndAt;
    private Integer regPresentCid;
    private Long forwardingBeginAt;
    private Long forwardingEndAt;
    private String direction;
    private String remark;
    private String parkingMerchant;
    private String parkingMerchantId;
    private String parkingCode;
    private String parkingCouponName;
}