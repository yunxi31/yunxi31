package com.itheima;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@ServletComponentScan
@EnableTransactionManagement
@SpringBootApplication//表示这是一个启动类的注解，需要配合导入springbootConfigure依赖
public class ReggieApplication {
    public static void main(String[] args) {
      SpringApplication.run(ReggieApplication.class,args);
      log.info("项目启动成功。。。");
    }
}