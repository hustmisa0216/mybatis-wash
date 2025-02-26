package com.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charge.entity.CommodityOrder;
import com.wash.entity.data.CommodityOrderConsumeTb;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommodityOrderMapper extends BaseMapper<CommodityOrder> {

}
