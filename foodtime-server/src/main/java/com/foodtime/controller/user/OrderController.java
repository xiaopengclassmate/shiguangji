package com.foodtime.controller.user;

import com.foodtime.dto.OrdersPaymentDTO;
import com.foodtime.dto.OrdersSubmitDTO;
import com.foodtime.result.PageResult;
import com.foodtime.result.Result;
import com.foodtime.service.OrderService;
import com.foodtime.vo.OrderPaymentVO;
import com.foodtime.vo.OrderSubmitVO;
import com.foodtime.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/order")
@ApiOperation("用户订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @ApiOperation("用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO =orderService.submitOrder(ordersSubmitDTO);

        return Result.success(orderSubmitVO);

    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        // 支付成功状态由支付宝异步回调(/notify/paySuccess)触发，此处不应直接调用 paySuccess
        return Result.success(orderPaymentVO);
    }
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetails(@PathVariable("id") Long id){
log.info("查询订单详情：{}", id);

OrderVO orderVO = orderService.getOrderDelails(id);

return Result.success(orderVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result<PageResult> page(int page,int pageSize,Integer status){
        log.info("查询历史订单：{}", status);
       PageResult pageResult = orderService.pageQueryUser(page,pageSize,status);
       return Result.success(pageResult);

    }
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        log.info("取消订单：{}", id);
        orderService.cancel(id);
        return Result.success();
    }
@PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        log.info("在来一单");
        orderService.repetieyion(id);
        return Result.success();

}

//客户催单
    @GetMapping("/reminder/{id}")
    @ApiOperation("客户催单")
    public Result reminder(@PathVariable Long id){
        log.info("客户催单");
        orderService.reminder(id);
        return Result.success();
    }


}
