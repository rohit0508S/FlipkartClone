package com.shoping.flipkart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FlipkartCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlipkartCloneApplication.class, args);
		System.out.println("Flipkart Clone !");
	}

}
