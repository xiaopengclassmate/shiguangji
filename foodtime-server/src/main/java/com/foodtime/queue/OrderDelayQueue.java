package com.foodtime.queue;

import com.foodtime.entity.Orders;
import com.foodtime.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.RedissonShutdownException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于Redisson的订单延迟队列
 * <p>
 * 用户下单后将订单ID投递到延迟队列，15分钟后自动消费：
 * - 若订单仍为"待付款"状态，则自动取消
 * - 若订单已支付，则跳过
 * <p>
 * 相比原来的定时任务轮询方案（每分钟扫描数据库），
 * 延迟队列方案优势：
 * 1. 精确到秒级超时，而非分钟级轮询
 * 2. 减少数据库无效查询压力
 * 3. 支持分布式部署，多实例不会重复消费
 */
@Component
@Slf4j
public class OrderDelayQueue {

    private static final String DELAY_QUEUE_NAME = "delay:order:timeout";
    private static final long TIMEOUT_MINUTES = 15;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderMapper orderMapper;

    private Thread consumerThread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * 投递订单到延迟队列
     *
     * @param orderId 订单ID
     */
    public void sendOrderTimeout(Long orderId) {
        RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue(DELAY_QUEUE_NAME);
        RDelayedQueue<Long> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        delayedQueue.offer(orderId, TIMEOUT_MINUTES, TimeUnit.MINUTES);
        log.info("订单[{}]已投递到延迟队列，{}分钟后自动检查超时", orderId, TIMEOUT_MINUTES);
    }

    /**
     * 应用启动后自动开启延迟队列消费者线程
     */
    @PostConstruct
    public void startConsumer() {
        consumerThread = new Thread(() -> {
            log.info("订单超时延迟队列消费者线程启动");
            RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue(DELAY_QUEUE_NAME);
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // 用poll带超时替代take，避免Redisson关闭时永久阻塞
                    Long orderId = blockingQueue.poll(5, TimeUnit.SECONDS);
                    if (orderId != null) {
                        processTimeoutOrder(orderId);
                    }
                } catch (InterruptedException e) {
                    log.info("延迟队列消费线程被中断，准备退出");
                    Thread.currentThread().interrupt();
                    break;
                } catch (RedissonShutdownException e) {
                    log.info("Redisson已关闭，延迟队列消费者退出");
                    break;
                } catch (Exception e) {
                    if (!running.get()) {
                        break;
                    }
                    log.error("处理超时订单异常", e);
                    // 异常后短暂休眠，避免疯狂报错
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            log.info("订单超时延迟队列消费者线程已停止");
        }, "order-delay-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    /**
     * 应用关闭时优雅停止消费者线程
     */
    @PreDestroy
    public void stopConsumer() {
        log.info("正在停止延迟队列消费者线程...");
        running.set(false);
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    /**
     * 处理超时订单
     * 若订单仍为待付款状态，则自动取消
     */
    private void processTimeoutOrder(Long orderId) {
        Orders order = orderMapper.getById(orderId);
        if (order == null) {
            log.warn("延迟队列消费：订单[{}]不存在，跳过", orderId);
            return;
        }

        // 仅处理待付款状态的订单
        if (Orders.PENDING_PAYMENT.equals(order.getStatus())) {
            Orders updateOrder = Orders.builder()
                    .id(orderId)
                    .status(Orders.CANCELLED)
                    .cancelReason("订单超时未支付，系统自动取消")
                    .cancelTime(LocalDateTime.now())
                    .build();
            orderMapper.update(updateOrder);
            log.info("订单[{}]超时未支付，已自动取消", orderId);
        } else {
            log.debug("订单[{}]状态为{}，无需取消", orderId, order.getStatus());
        }
    }
}
