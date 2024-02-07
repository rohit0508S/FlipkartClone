package com.shoping.flipkart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoping.flipkart.entity.Customer;
import com.shoping.flipkart.entity.User;

public interface CustomerRepo extends JpaRepository<Customer, Integer>{

}
