package com.catzhang.im.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author crazycatzhang
 */
@SpringBootApplication(scanBasePackages = {"com.catzhang.im.service",
        "com.catzhang.im.common"})
@MapperScan("com.catzhang.im.service.*.dao.mapper")
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

}
