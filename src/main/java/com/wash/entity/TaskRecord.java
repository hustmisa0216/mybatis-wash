package com.wash.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/3/31
 * @Description
 */
@Data
public class TaskRecord {

    private int ven;
    Map<Integer, AtomicInteger> siteMap=new HashMap<>();
    private int date;

    public TaskRecord(int ven){
      this.ven=ven;
    }




}
