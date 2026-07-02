package com.foodtime.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.foodtime.constant.MessageConstant;
import com.foodtime.constant.StatusConstant;
import com.foodtime.context.BaseContext;
import com.foodtime.dto.*;
import com.foodtime.entity.*;
import com.foodtime.exception.AddressBookBusinessException;
import com.foodtime.exception.OrderBusinessException;
import com.foodtime.exception.ShoppingCartBusinessException;
import com.foodtime.mapper.*;
import com.foodtime.queue.OrderDelayQueue;
import com.foodtime.result.PageResult;
import com.foodtime.service.OrderService;
import com.foodtime.service.PaymentService;
import com.foodtime.vo.OrderPaymentVO;
import com.foodtime.vo.OrderStatisticsVO;
import com.foodtime.vo.OrderSubmitVO;
import com.foodtime.vo.OrderVO;
import com.foodtime.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private OrderDelayQueue orderDelayQueue;
    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //异常情况的处理（收货地址为空、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        //查询当前用户的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构造订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());

        //向订单表插入1条数据
        orderMapper.insert(order);

        //订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }

        //向明细表插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);

        //扣减菜品库存
        for (ShoppingCart cart : shoppingCartList) {
            if (cart.getDishId() != null) {
                Dish dish = dishMapper.getById(cart.getDishId());
                if (dish == null) {
                    throw new OrderBusinessException("菜品不存在");
                }
                Integer stock = dish.getStock();
                if (stock != null && stock >= 0) {
                    int newStock = stock - cart.getNumber();
                    if (newStock < 0) {
                        throw new OrderBusinessException("菜品[" + dish.getName() + "]库存不足");
                    }
                    dishMapper.updateStock(dish.getId(), newStock);
                    //库存为0自动停售
                    if (newStock == 0) {
                        Dish updateDish = Dish.builder()
                                .id(dish.getId())
                                .status(StatusConstant.DISABLE)
                                .build();
                        dishMapper.update(updateDish);
                    }
                }
            }
        }

        //清理购物车中的数据
        shoppingCartMapper.deleteByUserId(userId);

        //投递订单到延迟队列，15分钟后自动检查超时取消
        orderDelayQueue.sendOrderTimeout(order.getId());

        //封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        log.info("支付宝沙箱支付，订单号：{}", ordersPaymentDTO.getOrderNumber());
        return paymentService.pay(ordersPaymentDTO, userId);
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            log.error("支付回调订单不存在，订单号：{}", outTradeNo);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端推送消息
        Map map =new HashMap();
        map.put("type",1);
        map.put("data",ordersDB.getId());
        map.put("content","订单号："+ outTradeNo);

        String json =JSON.toJSONString( map);
        webSocketServer.sendToAllClient(json);

    }

    @Override
    public OrderVO getOrderDelails(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        //将该订单及其详情封装到OrderVO并返回
        OrderVO oderVO = new OrderVO();
        BeanUtils.copyProperties(orders, oderVO);
        oderVO.setOrderDetailList(orderDetailList);
        return oderVO;
    }

    @Override
    public PageResult pageQueryUser(int page, int pageSize, Integer status) {
        //设置分页
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        Page<Orders> pageResult = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = getOrderVoList(pageResult);

        return new PageResult(pageResult.getTotal(), orderVOList);
    }
    @Override
    public void cancel(Long id) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(id);



        //校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 待付款的订单直接取消
        if (ordersDB.getStatus().equals(Orders.PENDING_PAYMENT)) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("用户取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        } else if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //待接单状态下取消，需要进行退款
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("用户取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    //接单
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    //拒单
    @Override
    public void rejection(OrdersRejectionDTO rejectionDTO) {
     //根据id查询订单
        Orders orders = orderMapper.getById(rejectionDTO.getId());


        //订单只有在待接单的状态下才可以拒单
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        //拒单需要退款，根据id更新订单状态，拒单原因，取消时间
        Orders orders1 =new Orders();
        orders1.setId(orders.getId());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setRejectionReason(rejectionDTO.getRejectionReason());
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders1);
        }
//管理员取消订单
    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        //根据id查询订单
        Orders orderDB = orderMapper.getById(ordersCancelDTO.getId());

        //校验订单是否存在
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //校验订单状态，只有待接单或已接单状态才可取消
        if (orderDB.getStatus() > Orders.CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //管理端取消订单，设置取消原因和取消时间
        Orders orders = Orders.builder()
                .id(orderDB.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {

        //获取当前订单id
        Orders orderDB = orderMapper.getById(id);
        //判断订单状态是否为已接单（待派送）
        if (orderDB == null || !orderDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        }
        //更新订单状态，状态转为派送中
        Orders orders =new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);

    }
//完成订单
    @Override
    public void complete(Long id) {
        //获取当前订单id
        Orders orderDB = orderMapper.getById(id);
        //判断订单状态是否为派送中
        if (orderDB == null || !orderDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单状态，状态转为已完成
        Orders orders =new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
//客户催单
    @Override
    public void reminder(Long id) {
        Orders orders =  orderMapper.getById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map =new HashMap();
        map.put("orderId",id);
        map.put("type",2);
        map.put("content","Order #" + orders.getNumber() + " is being processed, please wait");

        webSocketServer.sendToAllClient(JSON.toJSONString( map));
    }


    private  List<OrderVO> getOrderVoList(Page<Orders> page){
        List<OrderVO> orderVOList =new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList){
                OrderVO orderVO =new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                String orderDishes=getOrderDishesStr( orders);


                //将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);

            }

        }
        return orderVOList;
    }
    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3;）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 再来一单
     */
    @Override
    public void repetieyion(Long id) {
        // 查询当前订单
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 查询订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将订单详情转换为购物车对象
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = new ArrayList<>();

        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        // 批量插入购物车
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.conditionSearch(ordersPageQueryDTO);
        List<OrderVO> orderVOList = getOrderVoList(page);
        return new PageResult(page.getTotal(), orderVOList);
    }
}
