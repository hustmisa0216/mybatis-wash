package com.charge.entity;

import lombok.Data;

import java.math.BigInteger;
@Data
public class ChargeOrder {
    private int id;
    private String orderId;
    private int uid;
    private int siteId;
    private int plugId;
    private int port;
    private String couponId;
    private int duration;
    private byte chargeMode;
    private Float power;
    private Integer price;
    private Integer paymentType;
    private int paymentCoupon;
    private int paymentBalance;
    private Integer paymentTotal;
    private byte status;
    private int chargingStatus;
    private Long startAt;
    private Long stopAt;
    private int chargingMinutes;
    private String transactionId;
    private Integer timeConsumed;
    private int energyConsumed;
    private String powerRange;
    private int flag;
    private String extra;
    private Long createdAt;
    private Long updatedAt;

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getPlugId() {
        return plugId;
    }

    public void setPlugId(int plugId) {
        this.plugId = plugId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public byte getChargeMode() {
        return chargeMode;
    }

    public void setChargeMode(byte chargeMode) {
        this.chargeMode = chargeMode;
    }

    public Float getPower() {
        return power;
    }

    public void setPower(Float power) {
        this.power = power;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public int getPaymentCoupon() {
        return paymentCoupon;
    }

    public void setPaymentCoupon(int paymentCoupon) {
        this.paymentCoupon = paymentCoupon;
    }

    public int getPaymentBalance() {
        return paymentBalance;
    }

    public void setPaymentBalance(int paymentBalance) {
        this.paymentBalance = paymentBalance;
    }

    public Integer getPaymentTotal() {
        return paymentTotal;
    }

    public void setPaymentTotal(Integer paymentTotal) {
        this.paymentTotal = paymentTotal;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public int getChargingStatus() {
        return chargingStatus;
    }

    public void setChargingStatus(int chargingStatus) {
        this.chargingStatus = chargingStatus;
    }

    public Long getStartAt() {
        return startAt;
    }

    public void setStartAt(Long startAt) {
        this.startAt = startAt;
    }

    public Long getStopAt() {
        return stopAt;
    }

    public void setStopAt(Long stopAt) {
        this.stopAt = stopAt;
    }

    public int getChargingMinutes() {
        return chargingMinutes;
    }

    public void setChargingMinutes(int chargingMinutes) {
        this.chargingMinutes = chargingMinutes;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getTimeConsumed() {
        return timeConsumed;
    }

    public void setTimeConsumed(Integer timeConsumed) {
        this.timeConsumed = timeConsumed;
    }

    public int getEnergyConsumed() {
        return energyConsumed;
    }

    public void setEnergyConsumed(int energyConsumed) {
        this.energyConsumed = energyConsumed;
    }

    public String getPowerRange() {
        return powerRange;
    }

    public void setPowerRange(String powerRange) {
        this.powerRange = powerRange;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
