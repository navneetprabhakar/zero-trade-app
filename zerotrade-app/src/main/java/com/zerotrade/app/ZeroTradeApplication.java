package com.zerotrade.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.zerotrade" })
public class ZeroTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeroTradeApplication.class, args);
    }
}
