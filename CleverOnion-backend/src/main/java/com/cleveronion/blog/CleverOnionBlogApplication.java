package com.cleveronion.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CleverOnion 博客系统启动类
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class CleverOnionBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleverOnionBlogApplication.class, args);
    }

}