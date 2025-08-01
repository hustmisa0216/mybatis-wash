package com.wash.controller.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/7/28
 * @Description
 */
public class ShopMapping {

    public static void main(String[] args) {
// 替换为实际的 CSV 文件路径
        String csvFile = "d://mogo/start.csv";
        String line;
        String csvSplitBy = ",";

        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // 逐行读取 CSV 文件
            while ((line = br.readLine()) != null) {
                // 使用逗号分割每行数据
                String[] data = line.split(csvSplitBy);
                for (String value : data) {
                     map.put(data[0],data[1]);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        String shopFile = "d://mogo//shop.csv";
        String lineshop;
        String csvSplitByshop = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(shopFile))) {
            // 逐行读取 CSV 文件
            while ((lineshop = br.readLine()) != null) {
                // 使用逗号分割每行数据
                String[] data = lineshop.split(csvSplitByshop);
                String shopName=data[0];
                String shopId=data[2];
                System.out.println(lineshop);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
