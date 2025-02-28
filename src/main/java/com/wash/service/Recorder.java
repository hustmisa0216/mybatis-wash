package com.wash.service;

import com.alibaba.fastjson.JSON;
import com.wash.entity.ModifierData;
import com.wash.entity.Series;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.VendorProfitSharingTb;
import com.wash.entity.franchisee.FranchiseeSiteTb;
import com.wash.entity.statistics.FaSettlementTb;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;


@Service
public class Recorder {
    public  static final String FILE_PATH = "D:\\mogo\\wash\\";

    public void record(int vendorId, FaSettlementTb faSettlementTbRes, FranchiseeSiteTb franchiseeSiteTb, ModifierData modifierData)
            throws Exception {

        int siteId = franchiseeSiteTb.getSiteId();
        createFilesForVendor(vendorId, siteId, faSettlementTbRes.getDate());

        String path = buildFileFolder(vendorId, siteId, faSettlementTbRes.getDate());
        FileWriter payWriter = new FileWriter(path + FilesEnum.PAYTB_DATA.getFileName(), true);
        FileWriter commodityOrderWriter = new FileWriter(path + FilesEnum.COMMODITY_ORDER_DATA.getFileName(), true);
        FileWriter orderWriter = new FileWriter(path + FilesEnum.ORDERSTB_DATA.getFileName(), true);
        FileWriter vendorProfitWriter = new FileWriter(path + FilesEnum.VENDOR_PROFIT_DATA.getFileName(), true);
        FileWriter parentVpd = new FileWriter(path + FilesEnum.VENDOR_PROFIT_DATA_PARENT.getFileName(), true);

        FileWriter modifierWriter = new FileWriter(path+ FilesEnum.SERIES_JSON.getFileName(), true);
        payWriter.append("recordDate:"+faSettlementTbRes.getDate()+"\n");
        payWriter.flush();
        commodityOrderWriter.append("recordDate:"+faSettlementTbRes.getDate()+"\n");
        commodityOrderWriter.flush();
        orderWriter.append("recordDate:"+faSettlementTbRes.getDate()+"\n");
        orderWriter.flush();
        vendorProfitWriter.append("recordDate:"+faSettlementTbRes.getDate()+"\n");
        vendorProfitWriter.flush();
        parentVpd.append("recordDate:"+faSettlementTbRes.getDate()+"\n");
        parentVpd.flush();
        modifierWriter.append("recordDate:"+faSettlementTbRes.getDate()+"\n");
        modifierWriter.flush();


        modifierWriter.append(JSON.toJSONString(modifierData));
        modifierWriter.flush();
        for(Series series:modifierData.getSeriesList()){
            payWriter.append(series.getPayTb().toString()+"\n");
            payWriter.flush();

            commodityOrderWriter.append(series.getCommodityOrderTb().toString()+"\n");
            commodityOrderWriter.flush();
            for(OrdersTb ordersTb:series.getOrdersTbs()){
                orderWriter.append(ordersTb.toString()+"\n");
                orderWriter.flush();
            }
            for(VendorProfitSharingTb vendorProfitSharingTb:series.getVendorProfitSharingTbs()){
                vendorProfitWriter.append(vendorProfitSharingTb.toString()+"\n");
                vendorProfitWriter.flush();
            }

            if(CollectionUtils.isNotEmpty(series.getParentVendorProfitSharingTbs())) {
                for (VendorProfitSharingTb vendorProfitSharingTb : series.getParentVendorProfitSharingTbs()) {
                    parentVpd.append(vendorProfitSharingTb.toString() + "\n");
                    parentVpd.flush();
                }
            }


        }

    }

    public static void createFilesForVendor(int vendorId, int siteId, Integer date) {
        // 定义文件夹路径
        String folderPath = buildFileFolder(vendorId, siteId, date);
        File vendorSiteFolder = new File(folderPath);

        // 创建文件夹，如果存在则不创建
        if (!vendorSiteFolder.exists()) {
            if (vendorSiteFolder.mkdirs()) {
                System.out.println("文件夹创建成功: " + vendorSiteFolder.getAbsolutePath());
            } else {
                System.out.println("文件夹创建失败: " + vendorSiteFolder.getAbsolutePath());
                return;
            }
        } else {
            System.out.println("文件夹已存在: " + vendorSiteFolder.getAbsolutePath());
        }

        // 定义要创建的文件
        String[] fileNames = Arrays.stream(FilesEnum.values())
                .map(FilesEnum::getFileName)
                .toArray(String[]::new);

        for (String fileName : fileNames) {
            File file = new File(vendorSiteFolder, fileName);
            // 创建文件，如果存在则不创建
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        System.out.println("文件创建成功: " + file.getAbsolutePath());
                    } else {
                        System.out.println("文件创建失败: " + file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.out.println("创建文件时发生错误: " + e.getMessage());
                }
            } else {
                System.out.println("文件已存在: " + file.getAbsolutePath());
            }
        }
    }

    public static String buildFileFolder(int vendorId, int siteId, Integer date) {
        return FILE_PATH + vendorId + "/" + siteId + "/" + date+"/"; // 替换为实际路径
    }
    public static String buildAllPath(int vendorId) {
        return FILE_PATH + vendorId+"/"; // 替换为实际路径
    }
}
