package com.baomidou.mybatisplus.samples.crud.select;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.samples.crud.entity.PayTb;
import com.baomidou.mybatisplus.samples.crud.mapper.PayTbMapper;
import org.apache.ibatis.javassist.bytecode.SourceFileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Select {

    private String vendorId="";
    private String siteId="";
    private int amount=30;
    private String date="";
    @Autowired
    private PayTbMapper payTbMapper;

    @PostConstruct
    public void select(){
        QueryWrapper
        PayTb payTb=payTbMapper.selectById(5);
        System.out.println(payTb);
    }
}
