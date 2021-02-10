package io.seata.sample.service;

import io.seata.sample.entity.Order;
import java.math.BigDecimal;

public interface OrderService {

    /**
     * 创建订单
     *
     * @param order
     * @return
     */
    void create(Order order);

    /**
     * 修改订单状态
     *
     * @param userId
     * @param productId
     * @param status
     */
    void update(Long userId, Long productId, Integer status);

    Order detail(Order order);
}
