package com.charge.service;

import com.alibaba.fastjson.JSON;
import com.charge.entity.CharEntity;
import com.charge.entity.CharModifier;
import com.charge.entity.ChargeOrder;
import com.charge.entity.VendorProfitSharing;
import com.wash.entity.Series;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.VendorProfitSharingTb;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/28
 * @Description
 */
@Service
public class CharRecorder {


    public static final String CHAR_FILE_PATH = "D:\\mogo\\charge\\";

    public void record(int vendorId, Integer selectDate, List<CharEntity> charEntities, CharModifier charModifier)
            throws Exception {
        createFilesForVendor(vendorId, selectDate);

        String path = buildFileFolder(vendorId, selectDate);
        FileWriter payWriter = new FileWriter(path + FilesEnum.PAYTB_DATA.getFileName(), true);
        FileWriter commodityOrderWriter = new FileWriter(path + FilesEnum.COMMODITY_ORDER_DATA.getFileName(), true);
        FileWriter orderWriter = new FileWriter(path + FilesEnum.ORDERSTB_DATA.getFileName(), true);
        FileWriter vendorProfitWriter = new FileWriter(path + FilesEnum.VENDOR_PROFIT_DATA.getFileName(), true);

        FileWriter modifierWriter = new FileWriter(path + FilesEnum.SERIES_JSON.getFileName(), true);
        payWriter.append("recordDate:" + selectDate + "\n");
        payWriter.flush();
        commodityOrderWriter.append("recordDate:" + selectDate + "\n");
        commodityOrderWriter.flush();
        orderWriter.append("recordDate:" + selectDate + "\n");
        orderWriter.flush();
        vendorProfitWriter.append("recordDate:" +selectDate + "\n");
        vendorProfitWriter.flush();

        modifierWriter.append("recordDate:" + selectDate + "\n");
        modifierWriter.flush();

        modifierWriter.append(JSON.toJSONString(charModifier));
        modifierWriter.flush();
        for (CharEntity charEntity : charEntities) {
            payWriter.append(charEntity.getPay().toString() + "\n");
            payWriter.flush();
            commodityOrderWriter.append(charEntity.getCommodityOrder().toString() + "\n");
            commodityOrderWriter.flush();
            for (ChargeOrder chargeOrder : charEntity.getChargeOrders()) {
                orderWriter.append(chargeOrder.toString() + "\n");
                orderWriter.flush();
            }
            for (VendorProfitSharing vendorProfitSharingTb : charEntity.getVendorProfitSharingList()) {
                vendorProfitWriter.append(vendorProfitSharingTb.toString() + "\n");
                vendorProfitWriter.flush();
            }
        }

    }

    public static void createFilesForVendor(int vendorId, Integer date) {
        // 定义文件夹路径
        String folderPath = buildFileFolder(vendorId, date);
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

    public static String buildFileFolder(int vendorId, Integer date) {
        return CHAR_FILE_PATH + vendorId + "/" + date + "/"; // 替换为实际路径
    }

}
