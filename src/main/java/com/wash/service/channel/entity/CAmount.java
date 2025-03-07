package com.wash.service.channel.entity;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/3/7
 * @Description
 */
@Data
public class CAmount {

    private AtomicInteger preAmount=new AtomicInteger(0);
    private AtomicInteger vipAmount=new AtomicInteger(0);

}
