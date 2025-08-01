package com.wash.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineTb {
    private Integer id;
    private Integer siteId;
    private String securityKey = "b5522478d17b5d7a9";
    private String name;
    private String serialNumber;
    private String code;
    private Byte model;
    private Byte rank;
    private Short version;
    private Byte status = 0;
    private Byte useStatus = 0;
    private String sn = "";
    private String machineSn = "";
    private Float waterPressure = 0f;
    private Float voltage = 0f;
    private Float voltagePump = 0f;
    private Float electricityPump = 0f;
    private Float voltageCleaner = 0f;
    private Float electricityCleaner = 0f;
    private Float surplusFroth = 0f;
    private Float surplusWater = 0f;
    private Float temperatureCore = 0f;
    private Float temperatureMin = 0f;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
    private Byte light = 0;
    private String geoCode = "";
    private Double lat = 0.0;
    private Double lng = 0.0;
    private String address = "";
    private Long runHour = 281474976710655L;
    private Long installedAt = 0L;
    private Byte repairStatus = 0;
    private Long runHourWeekend = 281474976710655L;
    private Long offlineAt = 0L;
    private String levelSensor;
    private Integer groundLock;
    private Byte iot = 1;
    private String iccid;
    private String otaVersion;
    private Byte foamBrush = 0;
    private Long beginAt = 0L;
    private Byte mainboardType = 1;
    private String mainboardVersion;
    private Integer mainboardModule = 0;
}
