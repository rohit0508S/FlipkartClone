package com.shoping.flipkart.utility;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import com.shoping.flipkart.entity.User;
import com.shoping.flipkart.serviceImpl.AuthServiceImpl;

public class ScheduleJob {
private AuthServiceImpl authServiceImpl;
    @Scheduled(fixedDelay = 1000*60)
//	@Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    public void deleteNonVerifiedUsers() {
		authServiceImpl.deleteNonVerifiedUsers();		
    }
	
}
