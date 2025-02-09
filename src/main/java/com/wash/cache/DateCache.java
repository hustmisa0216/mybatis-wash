package com.wash.cache;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
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
        //从文件夹层级读取map
    }

}
