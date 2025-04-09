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
    private AtomicInteger curRe=new AtomicInteger(0);
    private AtomicInteger curIn=new AtomicInteger(0);
    private int dec;
    private double percent;

    public TaskRecord(int ven){
      this.ven=ven;
    }

    public  String genRecord(){
        return date + "," + ven + "," + dec+","+curRe.get()+"," +curIn.get()+ ","+percent+"%";
    }




}
