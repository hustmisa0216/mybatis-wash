package com.wash.service.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charge.entity.Pay;
import com.wash.entity.data.PayTb;
import com.wash.mapper.PayTbMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");


    @Autowired
    private PayTbMapper payTbMapper;


    public ResData create(int siteId,int num,int begin,int end){
       long time=System.currentTimeMillis()/1000;

       long beginTime=0;
       long endTime=0;
        try {
             beginTime=SIMPLE_DATE_FORMAT.parse(""+begin).getTime()/1000;
             endTime=SIMPLE_DATE_FORMAT.parse(""+end).getTime()/1000;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        QueryWrapper<PayTb> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("site_id",siteId);
        queryWrapper.eq("status",1)
                .ge("created_at",beginTime)
                .le("created_at",endTime);
        List<PayTb> listt=payTbMapper.selectList(queryWrapper);
        List<PayTb> ori=listt.stream().filter(i->i.getUid()>0).collect(Collectors.toList());

        double sum=ori.stream().mapToInt(PayTb::getAmount).sum()/100;
        List<PayTb> res=IntStream.range(0,ori.size())
                .filter(i->i%num==0)

                .mapToObj(i->ori.get(i))
                .map(i->{
                    String paysn=i.getPaySn();
                    i.setId(null);
                    i.setPaySn("23"+paysn.substring(1));
                    i.setUid(0);
                    i.setCreatedAt(i.getCreatedAt()+72*60*60);
                    return i;
                }
                ).collect(Collectors.toList());

        double addSum=res.stream().mapToInt(PayTb::getAmount).sum()/100;

        System.out.println("sum:"+sum+" addSum:"+addSum);
        this.saveBatch(res);
        return new ResData((int)sum,(int)addSum);

    }

}
