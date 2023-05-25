package cn.crisp.crispmaintenanceorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class CrispMaintenanceOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrispMaintenanceOrderApplication.class, args);
    }

}
