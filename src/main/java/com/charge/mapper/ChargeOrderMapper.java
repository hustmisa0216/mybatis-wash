package com.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charge.entity.ChargeOrder;
import com.charge.entity.CommodityOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChargeOrderMapper extends BaseMapper<ChargeOrder> {

}
