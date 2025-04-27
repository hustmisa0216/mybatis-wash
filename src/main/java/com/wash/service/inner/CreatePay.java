package com.wash.service.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charge.entity.Pay;
import com.wash.entity.data.PayTb;
import com.wash.mapper.PayTbMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/4/27
 * @Description
 */
@Service
public class CreatePay extends ServiceImpl<PayTbMapper,PayTb>  {


    @Autowired
    private PayTbMapper payTbMapper;


    public void create(int siteId,int num){
       long time=System.currentTimeMillis()/1000;

        QueryWrapper<PayTb> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("site_id",siteId);
        queryWrapper.eq("status",1)
                .ge("created_at",time-24*60*60*1080);
        List<PayTb> list=payTbMapper.selectList(queryWrapper);

        List<PayTb> res=IntStream.range(0,list.size())
                .filter(i->i%num==0)
                .mapToObj(i->list.get(i))
                .map(i->{
                    String paysn=i.getPaySn();
                    i.setPaySn("1"+paysn.substring(1));
                    i.setUid(0);
                    i.setCreatedAt(i.getCreatedAt()+24*60*60);
                    return i;
                }
                ).collect(Collectors.toList());

         this.saveBatch(res);

    }

}
