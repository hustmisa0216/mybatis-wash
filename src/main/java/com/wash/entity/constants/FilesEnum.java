package com.wash.entity.constants;

import lombok.Getter;

import javax.swing.text.MaskFormatter;
@Getter
public enum FilesEnum {

    PAYTB_DATA("付款","paytb.csv"),
    ORDERSTB_DATA("xc订单","orderstb.csv"),
    VENDOR_PROFIT_DATA("VPD","vendorProfit.csv"),

    VENDOR_PROFIT_DATA_PARENT("PVPD","parent_vendorProfit.csv"),
    COMMODITY_ORDER_DATA("cod文件","commodityOrder.csv"),
    SERIES_JSON("Series","series.csv"),
    //SQLS("语句","update.csv"), 未实现
    DATE("成功后日期","date.csv");

    private String msg;
    private String fileName;

    FilesEnum(String msg,String fileName){
        this.msg= msg;
        this.fileName=fileName;
    }


}
