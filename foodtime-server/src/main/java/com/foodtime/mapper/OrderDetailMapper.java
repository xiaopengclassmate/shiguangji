package com.foodtime.mapper;

import com.foodtime.entity.OrderDetail;
import com.foodtime.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface OrderDetailMapper {


    void insertBatch(List<OrderDetail> orderDetailsList);
@Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long id);
}
