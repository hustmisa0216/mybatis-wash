package com.wash.service.date;

import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/10
 * @Description
 */
@Service
public class DateGenerator {


    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_DATE = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyyMMdd"));

    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_DATEMONTH = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyyMM"));

    public  void generateDate(Object o){
        try {
            // 获取 createdAt 字段
            Field createdAtField = o.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true); // 设置为可访问

            // 获取 date 字段
            Field dateField = o.getClass().getDeclaredField("date");
            dateField.setAccessible(true); // 设置为可访问

            // 获取 date 字段
            Field dateMonthField = o.getClass().getDeclaredField("dateMonth");
            dateMonthField.setAccessible(true); // 设置为可访问

            // 获取 createdAt 的值
            Long createdAtValue = (Long) createdAtField.get(o);
            if (createdAtValue != null) {
                // 转换为日期字符串
                String dateValue = THREAD_LOCAL_DATE.get().format(new Date(createdAtValue*1000));
                // 设置 date 字段的值
                dateField.set(o, dateValue);

                String dateMonthValue = dateValue.substring(0,6);
                // 设置 date 字段的值
                dateMonthField.set(o, dateMonthValue);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // 处理异常
        }
    }
}
