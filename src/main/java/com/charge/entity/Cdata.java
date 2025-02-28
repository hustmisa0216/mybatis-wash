package com.charge.entity;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/28
 * @Description
 */
@Data
public class Cdata {

    private AtomicInteger amount=new AtomicInteger(0);
    private AtomicInteger re_count=new AtomicInteger(0);
    private AtomicInteger charge_count=new AtomicInteger(0);
    private AtomicInteger income=new AtomicInteger(0);
    private AtomicInteger consume=new AtomicInteger(0);

}
