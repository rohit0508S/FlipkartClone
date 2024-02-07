package com.shoping.flipkart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoping.flipkart.entity.Seller;
import com.shoping.flipkart.entity.User;

public interface SellerRepo extends JpaRepository<Seller, Integer>{

}
