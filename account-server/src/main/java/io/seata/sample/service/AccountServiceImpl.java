package io.seata.sample.service;

import io.seata.sample.dao.AccountDao;
import io.seata.sample.feign.OrderApi;
import io.seata.sample.result.Back;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("accountServiceImpl")
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private OrderApi orderApi;

    /**
     * 扣减账户余额
     *
     * @param userId 用户id
     * @param money  金额
     */
    @Override
    public void decrease(Long userId, BigDecimal money, Long productId) {
        LOGGER.info("------->扣减账户开始account中");
        accountDao.decrease(userId, money);
        LOGGER.info("------->扣减账户结束account中");
        //远程方法 修改订单状态
        LOGGER.info("修改订单状态开始");
        Back mes = orderApi.update(userId, productId, 0);
        LOGGER.info("修改订单状态结束：{}", mes);
    }
}
