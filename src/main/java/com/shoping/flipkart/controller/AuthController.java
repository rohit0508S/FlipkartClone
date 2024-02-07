package com.shoping.flipkart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoping.flipkart.request.UserRequest;
import com.shoping.flipkart.response.UserResponse;
import com.shoping.flipkart.service.AuthService;
import com.shoping.flipkart.utility.ResponseStructure;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {	
	
	AuthService authService;
@PostMapping("/register")
public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@RequestBody UserRequest userRequest){
	return authService.registerUser(userRequest);
}
}