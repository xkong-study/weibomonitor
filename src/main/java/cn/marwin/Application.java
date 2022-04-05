package cn.marwin;

import cn.marwin.classifier.MyClassifier;
import cn.marwin.crawler.CrawlTask;
//import cn.marwin.service.WebSecurityConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.*;

@SpringBootApplication
@MapperScan(value = {"cn.marwin.dao"})
public class Application {

    public static void main(String[] args) throws IOException {
        // 运行Web服务
        SpringApplication.run(Application.class, args);
        // 初始化分类器
       // new WebSecurityConfig();
        MyClassifier.init();
//        new WebSocketServer();
        // 设置定时爬虫任务
        Timer timer = new Timer();
        long delay = 1000; // 延迟启动时间
        long period = 1000 * 60 * 10; // 运行周期30m
        timer.scheduleAtFixedRate(new CrawlTask(), delay, period);
        }
}


