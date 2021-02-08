package io.seata.sample.feign;

import io.seata.sample.entity.Order;
import io.seata.sample.result.Back;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "order-server")
public interface OrderApi {

    /**
     * 修改订单金额
     *
     * @param userId
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "/order/update", method = RequestMethod.PUT)
    Back update(@RequestParam("userId") Long userId, @RequestParam("productId") Long productId, @RequestParam("status") Integer status);

    @RequestMapping(value = "/order/detail", method = RequestMethod.GET)
    Back<Order> getOrder(@RequestParam Order order);
}
