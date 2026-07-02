package com.foodtime.controller.notity;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.foodtime.properties.AlipayProperties;
import com.foodtime.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付回调通知
 */
@RestController
@RequestMapping("/notify")
@Slf4j
public class PayNotifyController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayProperties alipayProperties;

    /**
     * 支付宝支付成功异步通知
     */
    @PostMapping("/paySuccess")
    public String paySuccessNotify(HttpServletRequest request) {
        log.info("收到支付宝支付回调");

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );

            if (signVerified) {
                String outTradeNo = params.get("out_trade_no");
                String tradeStatus = params.get("trade_status");

                log.info("支付宝验签通过，订单号：{}，交易状态：{}", outTradeNo, tradeStatus);

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    orderService.paySuccess(outTradeNo);
                    log.info("订单支付成功处理完成：{}", outTradeNo);
                }

                return "success";
            } else {
                log.error("支付宝验签失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
            return "failure";
        }
    }
}
