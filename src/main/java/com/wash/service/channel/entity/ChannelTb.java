package com.wash.service.channel.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChannelTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id; // 主键ID
    private String name; // 渠道名称
    private Float divideProp; // 分成比例
    private String description; // 描述
    private String selfSite; // 自有网站
    private Long beginTime; // 合作开始时间
    private Long createdAt; // 创建时间
    private Long updatedAt; // 更新时间
    private Long deletedAt; // 删除时间
    private Boolean isRemoveOtherFee; // 是否排除水电费
    private Integer type; // 类型 0-合作渠道 1-普通加盟 2-联合经营 3-代理商
    private Integer parentId; // 父级ID


}
