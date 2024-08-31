package com.bless.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bless.message.dao.mapper")
public class MessageStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageStoreApplication.class, args);
    }


}


