package com.foodtime.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.foodtime.dto.OrdersPaymentDTO;
import com.foodtime.entity.Orders;
import com.foodtime.exception.OrderBusinessException;
import com.foodtime.mapper.OrderMapper;
import com.foodtime.properties.AlipayProperties;
import com.foodtime.service.PaymentService;
import com.foodtime.vo.OrderPaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class AlipayPaymentServiceImpl implements PaymentService {

    @Autowired
    private AlipayProperties alipayProperties;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public OrderPaymentVO pay(OrdersPaymentDTO ordersPaymentDTO, Long userId) throws Exception {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getAppPrivateKey(),
                "json", "UTF-8",
                alipayProperties.getAlipayPublicKey(),
                "RSA2"
        );

        // 查询订单真实金额
        Orders order = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (order == null) {
            throw new OrderBusinessException("订单不存在");
        }
        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new OrderBusinessException("无权操作此订单");
        }
        String amount = order.getAmount()
                .divide(new BigDecimal("100"))
                .toString();

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(ordersPaymentDTO.getOrderNumber());
        model.setTotalAmount(amount);
        model.setSubject("食光机外卖订单");
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        request.setBizModel(model);

        try {
            String form = alipayClient.pageExecute(request).getBody();
            log.info("支付宝支付页面生成成功，订单号：{}，金额：{}元", ordersPaymentDTO.getOrderNumber(), amount);

            OrderPaymentVO vo = new OrderPaymentVO();
            vo.setNonceStr(form);
            vo.setPaySign("alipay");
            vo.setPackageStr("page");
            vo.setSignType("RSA2");
            vo.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            return vo;
        } catch (AlipayApiException e) {
            log.error("支付宝支付请求失败", e);
            throw new RuntimeException("支付宝支付请求失败", e);
        }
    }
}
