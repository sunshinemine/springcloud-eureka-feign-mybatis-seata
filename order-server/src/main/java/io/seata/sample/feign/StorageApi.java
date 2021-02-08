package io.seata.sample.feign;

import io.seata.sample.result.Back;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author IT云清
 */
@FeignClient(value = "storage-server")
public interface StorageApi {

    /**
     * 扣减库存
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping(value = "/storage/decrease", method = RequestMethod.GET)
    Back decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
}
