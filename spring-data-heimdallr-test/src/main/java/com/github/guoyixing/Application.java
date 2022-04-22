package com.github.guoyixing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableHeimdallrJpaRepositories;

/**
 * @author 敲代码的旺财
 * @date 2022/4/18 14:41
 */
@SpringBootApplication
@EnableHeimdallrJpaRepositories
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
