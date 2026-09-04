package com.tanwei;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.tanwei.mapper")
@SpringBootApplication
public class TanWeiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TanWeiApplication.class, args);
    }

}
