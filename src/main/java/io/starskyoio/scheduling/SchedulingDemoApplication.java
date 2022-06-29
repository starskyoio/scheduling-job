package io.starskyoio.scheduling;

import io.starskyoio.scheduling.manager.SchedulingManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = {"io.starskyoio.scheduling.mapper"})
public class SchedulingDemoApplication implements CommandLineRunner {
    @Autowired
    private SchedulingManager schedulingManager;

    public static void main(String[] args) {
        SpringApplication.run(SchedulingDemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        schedulingManager.init();
    }
}
