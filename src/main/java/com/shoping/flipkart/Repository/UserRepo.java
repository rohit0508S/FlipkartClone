package com.shoping.flipkart.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoping.flipkart.entity.User;

public interface UserRepo extends JpaRepository<User, Integer>{
	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	List<User> findByIsEmailVerifiedFalse();
	
}
