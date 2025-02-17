package com.wash.cache;

import com.wash.service.Recorder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/7
 * @Description
 */
@Component
public class DateCache {

    public Map<Integer, Map<Integer, Set<Integer>>> SITE_DATE_MAP = new HashMap<>();

    @PostConstruct
    public void reload() throws IOException {
        File baseDir = new File(Recorder.FILE_PATH);

        //从文件夹层级读取map
        if (baseDir.isDirectory()) {
            File[] originDirectories = baseDir.listFiles(File::isDirectory);
            if (originDirectories ==null) {
                throw new RuntimeException("未读取到历史目录");
            }
            for (File vendorDir : originDirectories) {
                if (!vendorDir.isDirectory()) continue;
                File[] siteDirs = vendorDir.listFiles();
                if (siteDirs == null) continue;
                for (File siteDir : siteDirs) {
                    if (!siteDir.isDirectory()) continue;
                    File[] dateDirs = siteDir.listFiles();
                    if (dateDirs == null) continue;
                    for (File dateDir : dateDirs) {
                        if (!dateDir.isDirectory()) continue;
                        File[] files = dateDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                String absFileName = file.getAbsoluteFile().getName();
                                if (absFileName.equals("date.csv")) {
                                    // 提取 vendorId, siteId, date
                                    String path = file.getPath().replace(Recorder.FILE_PATH, "");
                                    long length=file.length();
                                    if (length> 0) {
                                        String[] pathParts = path.split("\\\\");
                                        if (pathParts.length >= 3) {
                                            int vendorId = Integer.valueOf(pathParts[0]); // 3273
                                            int siteId = Integer.valueOf(pathParts[1]); // 951
                                            int date = Integer.valueOf(pathParts[2]); // 20240509
                                            List<String> lines = Files.readAllLines(file.toPath());

                                            int totalAmount=0;
                                            int totalPay=0;
                                            for(String line:lines){
                                                if(line.contains("-")) {
                                                    String v[] = line.split("-");
                                                    if (v.length > 6) {
                                                        totalPay += Integer.valueOf(v[6]);
                                                        totalAmount += Integer.valueOf(v[7]);
                                                    }
                                                }
                                            }

                                            if(totalPay>280||totalAmount>108) {
                                                SITE_DATE_MAP
                                                        .computeIfAbsent(vendorId, k -> new HashMap<>())
                                                        .computeIfAbsent(siteId, k -> new HashSet<>())
                                                        .add(date);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        System.out.println(SITE_DATE_MAP);
        }
}
