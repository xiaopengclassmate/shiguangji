package com.foodtime.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.foodtime.dto.OrdersPaymentDTO;
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

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(ordersPaymentDTO.getOrderNumber());
        model.setTotalAmount("0.01"); // 沙箱固定1分钱测试
        model.setSubject("食光机外卖订单");
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        request.setBizModel(model);

        try {
            String form = alipayClient.pageExecute(request).getBody();
            log.info("支付宝支付页面生成成功，订单号：{}", ordersPaymentDTO.getOrderNumber());

            OrderPaymentVO vo = new OrderPaymentVO();
            vo.setNonceStr(form); // 用 nonceStr 字段存储支付HTML表单
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
