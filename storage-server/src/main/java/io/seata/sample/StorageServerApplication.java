package io.seata.sample;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 库存服务
 * @author wangzhongxiang
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@MapperScan("io.seata.sample.dao")
@EnableEurekaClient
@EnableFeignClients
public class StorageServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StorageServerApplication.class, args);
	}

}
