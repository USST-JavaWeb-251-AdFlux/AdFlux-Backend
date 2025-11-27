package com.usst.adfluxbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.usst.adfluxbackend.mapper")
public class AdFluxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdFluxBackendApplication.class, args);
        System.out.println("AdFlux-Backend ======启动成功======");
    }

}
