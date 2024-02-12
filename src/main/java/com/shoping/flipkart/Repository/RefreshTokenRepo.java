package com.shoping.flipkart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoping.flipkart.entity.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{

}
