package com.charge.entity;

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
public class ChargeTaskRecord {

    private int ven=0;
    Map<Integer, AtomicInteger> siteMap=new HashMap<>();
    private int date=0;
    private AtomicInteger curRe=new AtomicInteger(0);
    private AtomicInteger curIn=new AtomicInteger(0);
    private int dec=0;
    private double percent=0;

    public ChargeTaskRecord(int ven){
      this.ven=ven;
    }

    public  String genRecord(){
        return date + "  ,  " + ven + "  ,  " + dec+"  ,  "+curRe.get()+"  ,  " +curIn.get()+ "  ,  "+percent+"%";
    }


}
