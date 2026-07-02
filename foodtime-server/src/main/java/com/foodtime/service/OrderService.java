package com.foodtime.service;

import com.foodtime.dto.*;
import com.foodtime.result.PageResult;
import com.foodtime.vo.OrderPaymentVO;
import com.foodtime.vo.OrderStatisticsVO;
import com.foodtime.vo.OrderSubmitVO;
import com.foodtime.vo.OrderVO;

public interface OrderService {


    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    OrderVO getOrderDelails(Long id);

    PageResult pageQueryUser(int page, int pageSize, Integer status);

    void cancel(Long id);

    void repetieyion(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();

   void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);
//      管理员取消订单
    void adminCancel(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void complete(Long id);

    void reminder(Long id);
}
