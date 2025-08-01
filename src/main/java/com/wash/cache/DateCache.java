package com.wash.cache;

import com.alibaba.fastjson.JSON;
import com.charge.entity.Pay;
import com.charge.entity.SiteLatestData;
import com.wash.entity.ModifierData;
import com.wash.entity.Series;
import com.wash.entity.constants.FilesEnum;
import com.wash.entity.data.OrdersTb;
import com.wash.entity.data.PayTb;
import com.wash.service.Recorder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/7
 * @Description
 */
@Component
public class DateCache {

    public Map<Integer, Map<Integer, Set<Integer>>> SITE_DATE_MAP = new HashMap<>();
    public Map<Integer,Set<Integer>> C_DATE_MAP = new HashMap<>();

    public  static final String C_FILE_PATH = "D:\\mogo\\charge\\";

    @PostConstruct
    public void reload() {
        File baseDir = new File(Recorder.FILE_PATH);
        //从文件夹层级读取map

        Arrays.stream(baseDir.listFiles(File::isDirectory))
                .filter(File::isDirectory)
                .flatMap(vendorDir -> Arrays.stream(vendorDir.listFiles()))
                .filter(File::isDirectory)
                .flatMap(siteDir -> Arrays.stream(siteDir.listFiles()))
                .filter(File::isDirectory)
                .flatMap(dateDir -> Arrays.stream(dateDir.listFiles()))
                .filter(file -> file != null && FilesEnum.DATE.getFileName().equals(file.getAbsoluteFile().getName()))
                .forEach(file -> {
                    // 提取 vendorId, siteId, date
                    String path = file.getPath().replace(Recorder.FILE_PATH, "");
                    long length = file.length();
                    if (length > 0) {
                        String[] pathParts = path.split("\\\\");
                        if (pathParts.length >= 3) {
                            int vendorId = Integer.valueOf(pathParts[0]); // 3273
                            int siteId = Integer.valueOf(pathParts[1]); // 951
                            int date = Integer.valueOf(pathParts[2]); // 20240509
                            List<String> lines = null;
                            try {
                                lines = Files.readAllLines(file.toPath());
                            } catch (IOException e) {
                            }

                            if (CollectionUtils.isNotEmpty(lines)) {
                                int totalAmount = 0;
                                int totalPay = 0;
                                int all = 0;
                                for (int k = 0; k < lines.size(); k++) {
//vendorId+"-"+siteId+"||("+allPayCount+"-"+dayRechargeAmount+"-"+allIn+")||("+payCount+"-"+totalChargeAmount+"-"+totalIncome+")";

                                    String line = lines.get(k);

                                    if(StringUtils.isBlank(line)){
                                        continue;
                                    }
                                    if (line.contains("||")) {
                                        String v = line.split("\\|\\|")[2];
                                        if (k == 0) {
                                            String v1 = line.split("\\|\\|")[1];
                                            String vs1[] = v1.replace("(", "").replace(")", "").split("-");
                                            all = Integer.valueOf(vs1[1]);
                                        }
                                        String vs[] = v.replace("(", "").replace(")", "").split("-");
                                        if (vs.length > 2&&StringUtils.isNotBlank(vs[2])) {
                                            totalPay += Integer.valueOf(vs[1]);
                                            totalAmount += Integer.valueOf(vs[2]);
                                        }
                                    } else {//旧版本;
                                        if (line.contains("-")) {
                                            String v[] = line.split("-");
                                            if (v.length > 6) {
                                                totalPay += Integer.valueOf(v[6]);
                                                totalAmount += Integer.valueOf(v[7]);
                                            }
                                        }
                                    }
                                }

                                if (totalPay > 7600 || totalAmount > 7500) {
                                    SITE_DATE_MAP
                                            .computeIfAbsent(Integer.valueOf(vendorId), k -> new HashMap<>())
                                            .computeIfAbsent(Integer.valueOf(siteId), k -> new HashSet<>())
                                            .add(Integer.valueOf(date));
                                }
                                if (totalPay * 3 > all) {
                                    SITE_DATE_MAP
                                            .computeIfAbsent(Integer.valueOf(vendorId), k -> new HashMap<>())
                                            .computeIfAbsent(Integer.valueOf(siteId), k -> new HashSet<>())
                                            .add(Integer.valueOf(date));
                                }
                            }
                        }
                    }
                });
       // System.out.println(SITE_DATE_MAP);
    }

    @PostConstruct
    public void reloadC() throws IOException {
        File baseDir = new File(C_FILE_PATH);

        //从文件夹层级读取map
        if (baseDir.isDirectory()) {
            File[] originDirectories = baseDir.listFiles(File::isDirectory);
            if (originDirectories == null) {
                throw new RuntimeException("未读取到历史目录");
            }
            for (File vendorDir : originDirectories) {
                if (!vendorDir.isDirectory()) continue;
                File[] dateDirs = vendorDir.listFiles();
                if (dateDirs == null) continue;
                for (File dateDir : dateDirs) {
                    if (!dateDir.isDirectory()) continue;
                    File[] files = dateDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            String absFileName = file.getAbsoluteFile().getName();
                            if (absFileName.equals("date.csv")) {
                                // 提取 vendorId, date
                                String path = file.getPath().replace(C_FILE_PATH, "");
                                long length = file.length();
                                if (length > 0) {
                                    String[] pathParts = path.split("\\\\");
                                    if (pathParts.length >= 3) {
                                        int vendorId = Integer.valueOf(pathParts[0]); // 3273
                                        int date = Integer.valueOf(pathParts[1]); // 20240509
                                        List<String> lines = Files.readAllLines(file.toPath());

                                        int totalAmount = 0;
                                        int totalCount = 0;
                                        for (String line : lines) {
//selectDate+"||("+allCount+"-"+allAmount+")||("+secCount+"-"+amount+")||("+before+"-"+after+")";
                                            if (line.contains("||")) {
                                                String v = line.split("\\|\\|")[2];
                                                String vs[] = v.replace("(", "").replace(")", "").split("-");
                                                if (vs.length > 1) {
                                                    totalCount += Integer.valueOf(vs[0]);
                                                    totalAmount += Integer.valueOf(vs[1]);
                                                }
                                            } else {//旧版本;
                                                if (line.contains("-")) {
                                                    String v[] = line.split("-");
                                                    if (v.length > 6) {
                                                        totalCount += Integer.valueOf(v[6]);
                                                        totalAmount += Integer.valueOf(v[7]);
                                                    }
                                                }
                                            }
                                        }

                                        if (totalCount > 5 || totalAmount > 5800) {
                                            C_DATE_MAP
                                                    .computeIfAbsent(Integer.valueOf(vendorId), k -> new HashSet<>())
                                                    .add(Integer.valueOf(date));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }}

    public static void main(String[] args) throws IOException {
        String path="D:\\946\\";
        File baseDir = new File(path);
        File[] originDirectories = baseDir.listFiles(File::isDirectory);

        for(File file:originDirectories){
            if (file.isDirectory()) {
                //System.out.println(file.getName());
               // if(file.getName().equals("20231115")){
                File[] files = file.listFiles();
                for (File file1 : files) {
//                    if (file1.getName().contains("order")) {
//                        List<String> lines = Files.readAllLines(file1.toPath());
//                        for (String line : lines) {
//                            String v[] = line.split(",");
//                            if (v.length < 8) {
//                                continue;
//                            }
//                            OrdersTb ordersTb = OrdersTb.fromString(line);
//                            if (ordersTb.getUid().equals("10874213")) {
//                                System.out.println(ordersTb);
//                            }
//                             //   System.out.println(ordersTb);
//                        }
//                    }

                    if (file1.getName().contains("pay")) {
                        List<String> lines = Files.readAllLines(file1.toPath());
                        for (String line : lines) {
                            String v[] = line.split(",");
                            if (v.length < 2) {
                                continue;
                            }
                            PayTb paytb = PayTb.fromString(line);
                            if (paytb.getUid().intValue()==11567752){
                               System.out.println(paytb);
                            }
                            //System.out.println(paytb);
                        }
                    }
                }

            }
        }

    }
}
