package com.wash.recover;

import com.wash.entity.Series;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.CommodityOrdersTb;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.PayTb;
import com.wash.service.Recorder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/6/13
 * @Description
 */
@Component
public class ReByOrders {
    public  Set<Integer> uidSet = new HashSet<>();

    @PostConstruct
    public void as(){
        Executors.newCachedThreadPool().execute(()->{
        //    recover();
        });
    }

    public void recover(){
        {
            File baseDir = new File(Recorder.FILE_PATH);
            //从文件夹层级读取map

            Arrays.stream(baseDir.listFiles(File::isDirectory))
                    .filter(File::isDirectory)
                    .flatMap(vendorDir -> Arrays.stream(vendorDir.listFiles()))
                    .filter(File::isDirectory)
                    .flatMap(siteDir -> Arrays.stream(siteDir.listFiles()))
                    .filter(File::isDirectory)
                    .map(dateDir -> Arrays.stream(dateDir.listFiles()))
                    .forEach(i -> {
                        try {

                            long s1 = System.currentTimeMillis();
                            List<File> files = i.collect(Collectors.toList());
                            Map<Integer, Series> map = new HashMap<>();

                            for (File file : files) {
                                if (!StringUtils.endsWith(file.getName(), FilesEnum.PAYTB_DATA.getFileName())) {
                                    continue;
                                }
                                List<String> lines = null;
                                try {
                                    lines = Files.readAllLines(file.toPath());
                                    System.out.println("cost0:" + (System.currentTimeMillis() - s1));

                                } catch (IOException e) {
                                }
                                for (String line : lines) {
                                    if (line.split(",").length < 10) {
                                        continue;
                                    }
                                    if (StringUtils.endsWith(file.getName(), FilesEnum.ORDERSTB_DATA.getFileName())) {
//                                    OrdersTb ordersTb = OrdersTb.fromString(line);
//                                    if (map.containsKey(ordersTb.getUid().intValue())) {
//                                        Series series = map.get(ordersTb.getUid());
//                                        series.getOrdersTbs().add(ordersTb);
//
//                                    } else {
//                                        Series series = new Series();
//                                        series.setOrdersTbs(new ArrayList<>(Arrays.asList(ordersTb)));
//                                        map.put(ordersTb.getUid(), series);
//                                    }

                                    } else if (StringUtils.endsWith(file.getName(), FilesEnum.COMMODITY_ORDER_DATA.getFileName())) {
//                                    CommodityOrdersTb commodityOrdersTb = CommodityOrdersTb.fromString(line);
//                                    if (map.containsKey(commodityOrdersTb.getUid().intValue())) {
//                                        Series series = map.get(commodityOrdersTb.getUid());
//                                        series.setCommodityOrderTb(commodityOrdersTb);
//                                    } else {
//                                        Series series = new Series();
//                                        series.setCommodityOrderTb(commodityOrdersTb);
//                                        map.put(commodityOrdersTb.getUid(), series);
//                                    }
                                    } else if (StringUtils.endsWith(file.getName(), FilesEnum.PAYTB_DATA.getFileName())) {
                                        System.out.println("cost2:" + (System.currentTimeMillis() - s1));
                                        PayTb payTb = PayTb.fromString(line);
                                        uidSet.add(payTb.getUid());
                                        if (map.containsKey(payTb.getUid().intValue())) {
                                            Series series = map.get(payTb.getUid());
                                            series.setPayTb(payTb);
                                        } else {
                                            Series series = new Series();
                                            series.setPayTb(payTb);
                                            map.put(payTb.getUid(), series);
                                        }
                                    }
                                }
                            }
                            System.out.println("map:" + map.size());
                            // System.out.println("cost3:"+(System.currentTimeMillis()-s1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            System.out.println(uidSet.size());
        }
    }


}
