package com.wash.service.channel.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class ChannelSiteTb implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id; // 主键ID
    private Integer siteId; // 网站ID
    private Integer channelId; // 渠道ID
    private Long createdAt; // 创建时间
    private Long updatedAt; // 更新时间
    private Long deletedAt; // 删除时间

}
