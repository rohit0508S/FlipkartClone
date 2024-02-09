package com.shoping.flipkart.cacahe;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shoping.flipkart.entity.User;

@Configuration
public class CacheBeanConfig {
@Bean
public CacheStore<User> userCacaheStore(){
	return new CacheStore<User>(Duration.ofMinutes(5));
}

@Bean
public CacheStore<String> otpCacheStore(){
	return new CacheStore<String>(Duration.ofMinutes(5));
}


}
