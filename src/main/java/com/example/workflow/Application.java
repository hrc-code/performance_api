package com.example.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableTransactionManagement
@EnableScheduling
@ServletComponentScan
@SpringBootApplication()
@MapperScan("com.example.workflow.mapper")
public class Application {
  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
  }

}