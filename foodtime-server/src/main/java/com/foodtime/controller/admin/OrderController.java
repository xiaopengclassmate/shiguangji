package com.foodtime.controller.admin;

import com.foodtime.dto.OrdersCancelDTO;
import com.foodtime.dto.OrdersConfirmDTO;
import com.foodtime.dto.OrdersPageQueryDTO;
import com.foodtime.dto.OrdersRejectionDTO;
import com.foodtime.result.PageResult;
import com.foodtime.result.Result;
import com.foodtime.service.OrderService;
import com.foodtime.vo.OrderStatisticsVO;
import com.foodtime.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @GetMapping("conditionSearch")
    @ApiOperation("订单搜索接口")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索接口");
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderVOResult(@PathVariable Long id){
        log.info("查询订单详情：{}", id);
        OrderVO orderDelails = orderService.getOrderDelails(id);
        return Result.success(orderDelails);
    }
    @GetMapping("/statistics")
    @ApiOperation("各个订单的状态")
    public Result statistics(){
        log.info("各个订单的状态");
   OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
@PutMapping("/confirm")
    @ApiOperation("接受订单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接受订单：{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒绝订单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersConfirmDTO){
        log.info("拒绝订单：{}", ordersConfirmDTO);
        orderService.rejection(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.adminCancel(ordersCancelDTO);
        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id){
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id){
        orderService.complete(id);
        return Result.success();
    }
}
