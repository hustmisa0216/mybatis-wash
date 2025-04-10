package com.wash.entity.franchisee;

import lombok.Data;

import java.io.Serializable;

@Data
public class FaWithdrawTb implements Serializable {

    private Integer id;

    private Integer ownId;

    private Integer amount;

    private String paySn;

    private Integer status = 0;

    private String auditOpinion;

    private Long auditAt;

    private Integer settlementMode = 1;

    private String openid;

    private String userName;

    private String payee;

    private String cardNo;

    private String bankName;

    private Integer serviceCharge = 0;

    private String invoice;

    private String receipt;

    private Long createdAt;

    private Long updatedAt;

    private Long paidAt = 0L;
}