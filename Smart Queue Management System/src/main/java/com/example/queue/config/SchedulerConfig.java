package com.example.queue.config;

import com.example.queue.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private QueueService queueService;

    @Scheduled(fixedRate = 30000)
    public void expireTask() {
        try {
            queueService.expireOldTokens(3);
        } catch (Exception e) {
            System.out.println("Scheduler error: " + e.getMessage());
        }
    }
}
