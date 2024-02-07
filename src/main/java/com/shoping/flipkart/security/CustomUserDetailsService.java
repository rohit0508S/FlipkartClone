package com.shoping.flipkart.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.shoping.flipkart.Repository.UserRepo;
import com.shoping.flipkart.entity.User;
@Service
public class CustomUserDetailsService implements UserDetailsService{
	@Autowired
    private UserRepo userRepo;
    private User user;
    @Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByUsername(username).map((user)-> new CustomUserDetails(user)).orElseThrow(()->new RuntimeException("User not found "));
	}

}
