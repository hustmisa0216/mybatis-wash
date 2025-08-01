package com.wash.controller.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/7/18
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CostEntity {

    private int siteId;
    private int amount;
    private int machines;
    private int vendorId;
    private String vendorName;
    private String siteName;
    private int orderCount;


}
