package com.foodtime.service;

import com.foodtime.dto.OrdersPaymentDTO;
import com.foodtime.vo.OrderPaymentVO;

public interface PaymentService {
    /**
     * 生成支付页面
     * @param ordersPaymentDTO 订单支付信息
     * @param userId 用户ID
     * @return 支付页面HTML或URL
     */
    OrderPaymentVO pay(OrdersPaymentDTO ordersPaymentDTO, Long userId) throws Exception;
}
