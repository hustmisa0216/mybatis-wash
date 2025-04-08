package com.wash.config;

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
public class VenCalculator {

    private static final Map<Integer,Integer> proportion_map1=new HashMap<>();
    private static final Map<Integer,Integer> proportion_map2=new HashMap<>();
    private static final Map<Integer,Integer> proportion_map3=new HashMap<>();
    private static final Map<Integer,Integer> proportion_map4=new HashMap<>();

    private static final Map<Integer,Integer> constants_map1=new HashMap<>();
    private static final Map<Integer,Integer> constants_map2=new HashMap<>();
    private static final Map<Integer,Integer> constants_map3=new HashMap<>();
    private static final Map<Integer,Integer> constants_map4=new HashMap<>();

    private static final Map<Integer, Map<Integer, Integer>> categoryMap = new HashMap<>();

    @PostConstruct
    public void fillMap(){
        // 初始化proportion_map1
        proportion_map1.put(80, 4);
        proportion_map1.put(140, 5);
        proportion_map1.put(210, 6);
        proportion_map1.put(280, 7);
        proportion_map1.put(400, 8);
        proportion_map1.put(510, 9);
        proportion_map1.put(630, 10);
        proportion_map1.put(820, 11);
        proportion_map1.put(1080, 12);
        proportion_map1.put(1400, 13);
        proportion_map1.put(1800, 14);
        proportion_map1.put(2400, 15);
        proportion_map1.put(3200, 16);
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

    
    public double calculateAmount(double sum, double calSum,Map<Integer,Integer> pmap) {
        int divisor = 0;
        for (Map.Entry<Integer, Integer> entry : pmap.entrySet()) {
            if (calSum < entry.getKey()) {
                divisor = entry.getValue();
                break;
            }
        }
        return sum / divisor;
    }

    public Integer fromVen(int ven){
        Set<Integer> set1 = Arrays.stream("3191,3433,3353,3243,3250,3203,3229,3024".split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        Set<Integer> set2 = Arrays.stream(("3190,3362,3361,3300,3122,3258,3205,3177,3280,3390,3092,3308,3287,3283,3382,3225,3223,3100,3166" +
                        "").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        Set<Integer> set3 = Arrays.stream(("3073,3160,3089,3033,3265,3434,3325,3215,3117,3230,3248,3221,3392,3260," +
                        "3266,3231,3114,3351,3411,3188,3168,3310,3297,3313,3194,3083,8").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        Set<Integer> set4 = Arrays.stream("3323,3066,3453,3278,3291,3044,3422".split(","))
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
        return 3;//默认类别3

    }

    public static void main(String[] args) {

    }


}
