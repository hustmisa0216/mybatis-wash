package com.wash.cache;

import com.wash.service.Recorder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/2/7
 * @Description
 */
@Component
public class DateCache {

    public  Map<String, Map<String, Set<String>>> SITE_DATE_MAP=new HashMap<>();

    @PostConstruct
    public void reload(){
        File baseDir = new File(Recorder.FILE_PATH);

        //从文件夹层级读取map
        if (baseDir.isDirectory()) {
            File[] directories = baseDir.listFiles(File::isDirectory);
            if (directories != null) {
                for (File dir : directories) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().equals("ok.csv")) {
                                // 提取 vendorId, siteId, date
                                String[] pathParts = file.getParentFile().getName().split("\\\\");
                                if (pathParts.length >= 3) {
                                    String vendorId = pathParts[0]; // 3273
                                    String siteId = pathParts[1]; // 951
                                    String date = pathParts[2]; // 20240509

                                    // 存入 SITE_DATE_MAP
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
