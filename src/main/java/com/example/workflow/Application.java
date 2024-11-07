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
@SpringBootApplication
@MapperScan("com.example.workflow.mapper")
public class Application {
  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
    // http://patorjk.com/software/taag/?spm=5176.28103460.0.0.297c5d27o3B1lK 横幅内容生成
    System.out.println("                      _____                                                \n" +
            "______   ____________/ ____\\___________  _____ _____    ____   ____  ____  \n" +
            "\\____ \\_/ __ \\_  __ \\   __\\/  _ \\_  __ \\/     \\\\__  \\  /    \\_/ ___\\/ __ \\ \n" +
            "|  |_> >  ___/|  | \\/|  | (  <_> )  | \\/  Y Y  \\/ __ \\|   |  \\  \\__\\  ___/ \n" +
            "|   __/ \\___  >__|   |__|  \\____/|__|  |__|_|  (____  /___|  /\\___  >___  >\n" +
            "|__|        \\/                               \\/     \\/     \\/     \\/    \\/" + '\n'
            + "绩效管理系统后台服务启动成功");

  }

}