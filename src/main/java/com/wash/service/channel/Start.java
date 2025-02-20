package com.wash.service.channel;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.statistics.EnsureIncomeTb;
import com.wash.mapper.EnsureIncomeTbMapper;
import com.wash.mapper.OrdersTbMapper;
import com.wash.mapper.channel.ChannelSiteTbMapper;
import com.wash.service.channel.entity.ChannelSiteTb;
import com.wash.service.date.DateGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/20
 * @Description
 */
@Component
public class Start {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private ChannelSiteTbMapper channelSiteTbMapper;
    @Autowired
    private OrdersTbMapper ordersTbMapper;
    @Autowired
    private EnsureIncomeTbMapper ensureIncomeTbMapper;

    @Autowired
    private DateGenerator dateGenerator;

    public String start(int channelId, int startDate, int endDate) throws ParseException {
        QueryWrapper<ChannelSiteTb> channelSiteTbQueryWrapper=new QueryWrapper<>();
        channelSiteTbQueryWrapper.eq("channel_id",channelId)
                .isNull("deleted_at");

        List<ChannelSiteTb> channelSiteTbList=channelSiteTbMapper.selectList(channelSiteTbQueryWrapper);


        long dateTimeStart = SIMPLE_DATE_FORMAT.parse(startDate+"").getTime() / 1000;
        long dateTimeEnd = SIMPLE_DATE_FORMAT.parse(endDate+"").getTime() / 1000;



        for(ChannelSiteTb channelSiteTb:channelSiteTbList){
            int siteId=channelSiteTb.getSiteId();
            QueryWrapper<OrdersTb> ordersTbQueryWrapper=new QueryWrapper<>();
            ordersTbQueryWrapper.eq("site_id",siteId).eq("status",2)
                    .ge("created_at", dateTimeStart)
                    .le("created_at", dateTimeEnd);

            List<OrdersTb> ordersTbs=ordersTbMapper.selectList(ordersTbQueryWrapper);

            List<OrdersTb> viporders=ordersTbs.stream().filter(i->i.getComboType()==4).collect(Collectors.toList());
            List<OrdersTb> preOrders=ordersTbs.stream().filter(i->i.getComboType()==8).collect(Collectors.toList());

            viporders.stream().forEach(i->{
                dateGenerator.generateDate(i);
            });
            preOrders.stream().forEach(i->{
                dateGenerator.generateDate(i);
            });
            Map<Integer,List<OrdersTb>> vipDataMap=viporders.stream().collect(Collectors.groupingBy(OrdersTb::getDate));
            Map<Integer,List<OrdersTb>> preDataMap=preOrders.stream().collect(Collectors.groupingBy(OrdersTb::getDate));

            Map<Integer, List<OrdersTb>> vipDecMap = vipDataMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, // 保留原来的键
                            entry -> IntStream.range(0, entry.getValue().size()) // 获取索引范围
                                    .filter(i -> i % 2 == 0||i%5==0) // 过滤出索引为 3 的倍数
                                    .mapToObj(entry.getValue()::get) // 获取对应的订单对象
                                    .collect(Collectors.toList()) // 收集为列表
                    ));

            Map<Integer, List<OrdersTb>> preDecMap = preDataMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, // 保留原来的键
                            entry -> IntStream.range(0, entry.getValue().size()) // 获取索引范围
                                    .filter(i -> i % 2 == 0||i%5==0) // 过滤出索引为 3 的倍数
                                    .mapToObj(entry.getValue()::get) // 获取对应的订单对象
                                    .collect(Collectors.toList()) // 收集为列表
                    ));

            List<OrdersTb> res=vipDecMap.values().stream() // 获取所有的值
                    .flatMap(List::stream) // 将每个 List<OrdersTb> 展平为一个流
                    .collect(Collectors.toList());
            res.addAll(preDecMap.values().stream() // 获取所有的值
                    .flatMap(List::stream) // 将每个 List<OrdersTb> 展平为一个流
                    .collect(Collectors.toList()));

            for(int date:preDecMap.keySet()){
                List<OrdersTb> predicList=preDecMap.get(date);
                DoubleSummaryStatistics doubleSummaryStatistics=predicList.stream().mapToDouble(i->i.getPaymentPrepaid()).summaryStatistics();
                UpdateWrapper<EnsureIncomeTb> ensureIncomeTbQueryWrapper=new UpdateWrapper<>();
                ensureIncomeTbQueryWrapper.eq("site_id",siteId)
                        .eq("date",date)
                        .setSql("prepaid_money = prepaid_money-"+doubleSummaryStatistics.getSum())
                        .setSql("vip_money = vip_money *"+3+"/"+5);
                ensureIncomeTbMapper.update(null,ensureIncomeTbQueryWrapper);
            }

            if(CollectionUtils.isNotEmpty(res))
            ordersTbMapper.deleteBatchIds(res);
        }
        return null;
    }

}
