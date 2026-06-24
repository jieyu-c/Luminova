package cn.jieyuc.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class LuminovaUserApplication {
    public static void main(String[] args) {
        System.setProperty(
                "dubbo.network.interface.point-to-point.ignored",
                System.getProperty("dubbo.network.interface.point-to-point.ignored", "true")
        );
        SpringApplication.run(LuminovaUserApplication.class);
    }
}
