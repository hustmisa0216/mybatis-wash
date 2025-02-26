package com.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charge.entity.Pay;
import com.wash.entity.data.CommodityOrdersTb;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayMapper extends BaseMapper<Pay> {

}
