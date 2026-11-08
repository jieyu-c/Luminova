package cn.jieyuc.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 统一云文件服务启动入口。 */
@SpringBootApplication
public class LuminovaFileApplication {
    /** 启动云文件服务。 */
    public static void main(String[] args) {
        SpringApplication.run(LuminovaFileApplication.class, args);
    }
}
