package com.shoping.flipkart.service;

import org.springframework.http.ResponseEntity;

import com.shoping.flipkart.request.UserRequest;
import com.shoping.flipkart.response.UserResponse;
import com.shoping.flipkart.utility.ResponseStructure;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest);

}
