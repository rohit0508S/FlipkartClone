package com.shoping.flipkart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoping.flipkart.entity.AccessToken;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{

}