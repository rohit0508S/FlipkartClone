package com.shoping.flipkart.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoping.flipkart.entity.AccessToken;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{

	Optional<AccessToken> findByToken(String at);

	List<AccessToken> findAllByExpirationBefore(LocalDateTime currentTime);

	Optional<AccessToken> findByTokenAndIsBlocked(String at, boolean b);



}