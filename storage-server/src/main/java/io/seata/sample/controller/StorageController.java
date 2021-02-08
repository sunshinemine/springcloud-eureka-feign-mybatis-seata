package io.seata.sample.controller;


import io.seata.sample.result.Back;
import io.seata.sample.result.Result;
import io.seata.sample.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author IT云清
 */
@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageServiceImpl;

    /**
     * 扣减库存
     *
     * @param productId 产品id
     * @param count     数量
     * @return
     */
    @GetMapping("/decrease")
    public Back decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count) {
        storageServiceImpl.decrease(productId, count);
        return Back.noBody(Result.SUCCESS);
    }
}
