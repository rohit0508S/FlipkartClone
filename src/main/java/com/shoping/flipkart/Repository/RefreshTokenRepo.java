package com.shoping.flipkart.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoping.flipkart.entity.RefreshToken;
import com.shoping.flipkart.entity.User;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{

	Optional<RefreshToken> findByToken(String rt);

	List<RefreshToken> findAllByExpirationBefore(LocalDateTime currentTime);

	List<RefreshToken> findAllByUserAndIsBlockedAndTokenNot(User user, boolean b, String refreshToken);

}
