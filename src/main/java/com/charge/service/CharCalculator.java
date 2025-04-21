package com.charge.service;

import com.charge.entity.SelectInfo;
import com.wash.entity.DecData;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/4/8
 * @Description
 */
//融合阈值map计算
    //TODO计算过融合比例
    //计算追踪距离超参数阈值、针对车类别设定不同的阈值
    @Component
public class CharCalculator {

    private static final LinkedHashMap<Integer,Integer> proportion_map1=new LinkedHashMap<>();
    private static final LinkedHashMap<Integer,Integer> proportion_map2=new LinkedHashMap<>();
    private static final LinkedHashMap<Integer,Integer> proportion_map3=new LinkedHashMap<>();
    private static final LinkedHashMap<Integer,Integer> proportion_map4=new LinkedHashMap<>();

//    private static final Map<Integer,Integer> constants_map1=new HashMap<>();
//    private static final Map<Integer,Integer> constants_map2=new HashMap<>();
//    private static final Map<Integer,Integer> constants_map3=new HashMap<>();
//    private static final Map<Integer,Integer> constants_map4=new HashMap<>();

    private static final Map<Integer, LinkedHashMap<Integer, Integer>> categoryMap = new HashMap<>();

    @PostConstruct
    public void fillMap(){
        // 初始化proportion_map1
        proportion_map1.put(60, 5);
        proportion_map1.put(120, 6);
        proportion_map1.put(180, 7);
        proportion_map1.put(280, 8);
        proportion_map1.put(400, 9);
        proportion_map1.put(560, 10);
        proportion_map1.put(720, 11);
        proportion_map1.put(960, 12);
        proportion_map1.put(1000, 13);
        proportion_map1.put(1200, 14);
        proportion_map1.put(1600, 15);

        // 对于大于3200的情况，使用默认值
        proportion_map1.put(Integer.MAX_VALUE, 17);

        proportion_map2.put(80, 5);
        proportion_map2.put(140, 6);
        proportion_map2.put(210, 7);
        proportion_map2.put(280, 8);
        proportion_map2.put(400, 9);
        proportion_map2.put(510, 10);
        proportion_map2.put(630, 11);
        proportion_map2.put(820, 12);
        proportion_map2.put(1080, 13);
        proportion_map2.put(1400, 14);
        proportion_map2.put(1800, 15);
        proportion_map2.put(2400, 17);
        proportion_map2.put(3200, 18);
        // 对于大于3200的情况，使用默认值
        proportion_map2.put(Integer.MAX_VALUE, 19);



        proportion_map3.put(80, 6);
        proportion_map3.put(140, 7);
        proportion_map3.put(210, 8);
        proportion_map3.put(280, 9);
        proportion_map3.put(400, 10);
        proportion_map3.put(510, 11);
        proportion_map3.put(630, 12);
        proportion_map3.put(820, 13);
        proportion_map3.put(1080, 14);
        proportion_map3.put(1400, 15);
        proportion_map3.put(1800, 16);
        proportion_map3.put(2400, 17);
        proportion_map3.put(3200, 18);
        // 对于大于3200的情况，使用默认值
        proportion_map3.put(Integer.MAX_VALUE, 19);

        proportion_map4.put(80, 7);
        proportion_map4.put(140, 8);
        proportion_map4.put(210, 9);
        proportion_map4.put(280, 10);
        proportion_map4.put(400, 11);
        proportion_map4.put(510, 12);
        proportion_map4.put(630, 13);
        proportion_map4.put(820, 14);
        proportion_map4.put(1080, 15);
        proportion_map4.put(1400, 16);
        proportion_map4.put(1800, 17);
        proportion_map4.put(2400, 18);
        proportion_map4.put(3200, 19);
        // 对于大于3200的情况，使用默认值
        proportion_map4.put(Integer.MAX_VALUE, 20);

        categoryMap.put(1, proportion_map1);
        categoryMap.put(2, proportion_map2);
        categoryMap.put(3, proportion_map3);
        categoryMap.put(4, proportion_map4);
    }

    private static double getCalAmount(SelectInfo selectInfo, Integer inputAmount) {
        double vendorIncome= selectInfo.getAmount();
        double calAmount = 0;
        double calSum = vendorIncome / 100;

        if (calSum < 100) {
            calAmount = vendorIncome / 4;
        } else if (calSum < 200) {
            calAmount = vendorIncome / 5;
        } else if (calSum < 400) {
            calAmount = vendorIncome / 6;
        } else if (calSum < 600) {
            calAmount = vendorIncome / 7;
        } else if (calSum < 800) {
            calAmount = vendorIncome / 8;
        } else if (calSum < 2000) {
            calAmount = vendorIncome / 11;
        } else {
            calAmount = vendorIncome / 12;
        }

        if(inputAmount !=null){
            calAmount= inputAmount *100;
        }
        return calAmount;
    }
    
    public double calculateAmount(int venId, SelectInfo selectInfo, Integer inputDecAmount) {

        Map<Integer,Integer> proportionMap=categoryMap.get(1);
        double vendorIncome= selectInfo.getAmount();
        int divisor = 0;
        double calSum = vendorIncome / 100;

        for (Map.Entry<Integer, Integer> entry : proportionMap.entrySet()) {
            if (calSum < entry.getKey()) {
                divisor = entry.getValue();
                break;
            }
        }
        double calAmount = vendorIncome / divisor;
        int decAmount = inputDecAmount != null ? inputDecAmount : (int) calAmount;//程序内限制的amount,需要同事满足两个
        return  decAmount;
    }


    public int fromVen(int ven){
        Set<Integer> set1 = Arrays.stream("3225,3191,3433,3353,3243,3250,3203,3229,3024".split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        Set<Integer> set2 = Arrays.stream(("3190,3362,3361,3300,3122,3258,3205,3177,3280,3390,3092,3308,3287,3283,3382,3223,3100,3166,3114" +
                        "").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        Set<Integer> set3 = Arrays.stream(("3073,3160,3089,3033,3265,3325,3215,3117,3230,3248,3221,3392,3260," +
                        "3266,3231,3114,3351,3411,3188,3168,3310,3297,3313,3194,3083,8,3066").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        Set<Integer> set4 = Arrays.stream("3323,3453,3278,3291,3044,3422,3434".split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());


        if(set1.contains(ven)){
            return 1;
        }else if(set2.contains(ven)){
            return 2;
        }else if(set3.contains(ven)){
            return 3;
        }else if(set4.contains(ven)){
            return 4;
        }
        return 4;//默认类别3

    }

    //根据选定history 的计算额度
    public DecData calculateAmount(int inputVendorId,int size, double sum, Integer inputDecAmount) {

        double calAmount = 0;
        int inc=1;
        int incr=2;

        if(inputVendorId==3191){
            inc=-1;
            incr=-1;
        }
        if(inputVendorId==3362||inputVendorId==3122||inputVendorId==3433){
            inc=0;
            incr=0;
        }
        double calSum = sum / 100;
        if (calSum < 80) {
            calAmount = sum / (5+inc);
        } else if (calSum < 140) {
            calAmount = sum / (6+inc);
        } else if (calSum < 210) {
            calAmount = sum / (7+inc);
        } else if (calSum < 280) {
            calAmount = sum / (8+inc);
        } else if (calSum < 400) {
            calAmount = sum / (9+incr);
        } else if (calSum < 510) {
            calAmount = sum / (10+incr);
        } else if (calSum < 630) {
            calAmount = sum / (11+incr);
        } else if (calSum < 820) {
            calAmount = sum / (12+incr);
        } else if (calSum < 1080) {
            calAmount = sum / (13+incr);
        } else if (calSum < 1400) {
            calAmount = sum /(14+incr);
        } else if (calSum < 1800) {
            calAmount = sum / (15+incr);
        } else if (calSum < 2400) {
            calAmount = sum / (16+incr);
        } else if (calSum < 3200) {
            calAmount = sum / (17+incr);
        } else {
            calAmount = sum / (18+incr);
        }
        int decAmount = inputDecAmount != null ? inputDecAmount : (int) calAmount;//程序内限制的amount,需要同事满足两个
        return new DecData(sum, decAmount, inputDecAmount != null);
    }


    public static void main(String[] args) {

        double percent=Math.round((double) 79 * 10000 / 145)/100.0;

        System.out.println(percent);
        CharCalculator charCalculator = new CharCalculator();
        charCalculator.fillMap();
        double  dec = charCalculator.calculateAmount(3323, new SelectInfo(20241210,300), null);
        System.out.println(dec);
    }

}
