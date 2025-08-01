package com.wash.controller.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/7/22
 * @Description
 */
public class CostReadFile {


    public static void main(String[] args) {
        String filePath = "D://mogo/onceupdate.csv"; // 替换为实际的文件路径
        List<CostEntity> costEntities = readCostEntitiesFromFile(filePath);

        Map<String,String> cityMap=fromFile();

        Map<Integer, List<CostEntity>> siteIdToCostEntities = costEntities.stream()
                .collect(Collectors.groupingBy(CostEntity::getSiteId));

        for (Map.Entry<Integer, List<CostEntity>> entry : siteIdToCostEntities.entrySet()) {

             int tMachine=0;
             int sum=0;
             int orderCount=0;
             int washcount=0;
             String siteName="";
             String vendorId="";
             String vendorName="";

            Integer siteId = entry.getKey();
            Map<Integer,List<CostEntity>> amountMap=entry.getValue().stream().collect(Collectors.groupingBy(CostEntity::getAmount));

            for(int amount:amountMap.keySet()) {
                int amountSum=0;
                int amountwashcount=0;
                for (CostEntity costEntity : amountMap.get(amount)) {
                    amountSum += costEntity.getAmount();
                    amountwashcount += costEntity.getOrderCount();
                    tMachine=costEntity.getMachines();
                    siteName=costEntity.getSiteName();
                    vendorName=costEntity.getVendorName();
                    vendorId=costEntity.getVendorId()+"";
                }
                sum+=amountSum;
                washcount+=amountwashcount;
                orderCount+=amountMap.get(amount).size();
            }
            if(tMachine==0){
                tMachine=1;
            }
            System.out.println(vendorId+"-"+vendorName+"-"+siteName+"-"+siteId+"-"+cityMap.get(siteName)+"-"+tMachine+"-"+"all"+"-1-"+orderCount+"-"+sum/100+"-"+washcount);

            for(int amount:amountMap.keySet()) {
                int amountSum=0;
                int amountwashcount=0;
                for (CostEntity costEntity : amountMap.get(amount)) {
                    amountSum += costEntity.getAmount();
                    amountwashcount += costEntity.getOrderCount();
                }
                double sp=(double)amountSum/(double)sum;
                System.out.println(vendorId+"-"+vendorName+"-"+siteName+"-"+siteId+"-"+cityMap.get(siteName)+"-"+tMachine+"-"+amount+"-"+sp+"-"+amountMap.get(amount).size()+"-"+amountSum/100+"-"+amountwashcount);
            }
        }



    }

    private static Map<String, String> fromFile() {
        String filePath = "D://mogo/city.csv"; // 替换为实际的文件路径

        Map<String, String> cityMap=new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    String city = parts[2].trim();
                    cityMap.put(name, city);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityMap;
    }

    /**
     * 从指定文件中读取 CostEntity 信息并转换为 CostEntity 对象列表。
     *
     * @param filePath 包含 CostEntity 信息的文件路径
     * @return CostEntity 对象列表
     */
    public static List<CostEntity> readCostEntitiesFromFile(String filePath) {
        List<CostEntity> costEntities = new ArrayList<>();
        Pattern pattern = Pattern.compile("CostEntity\\(siteId=(\\d+), amount=(\\d+), " +
                "machines=(\\d+), vendorId=(\\d+), vendorName=(.*?), siteName=(.*?), orderCount=(\\d+)\\)");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    int siteId = Integer.parseInt(matcher.group(1));
                    int amount = Integer.parseInt(matcher.group(2));
                    int machines = Integer.parseInt(matcher.group(3));
                    int vendorId = Integer.parseInt(matcher.group(4));
                    String vendorName = matcher.group(5);
                    String siteName = matcher.group(6);
                    int orderCount = Integer.parseInt(matcher.group(7));

                    CostEntity entity = new CostEntity(siteId, amount, machines, vendorId, vendorName, siteName, orderCount);
                    costEntities.add(entity);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return costEntities;
    }

}
