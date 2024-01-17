package com.harvey.review_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 虎哥
 */
@MapperScan("com.harvey.review_system.mapper")
@SpringBootApplication
public class ReviewSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewSystemApplication.class, args);

    }

}
