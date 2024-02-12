package com.shoping.flipkart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoping.flipkart.request.AuthRequest;
import com.shoping.flipkart.request.OtpModel;
import com.shoping.flipkart.request.UserRequest;
import com.shoping.flipkart.response.AuthResponse;
import com.shoping.flipkart.response.UserResponse;
import com.shoping.flipkart.service.AuthService;
import com.shoping.flipkart.utility.ResponseStructure;

import jakarta.servlet.http.HttpServletResponse;
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

@PostMapping("/verify-otp")
public ResponseEntity<String> verifyOTP(@RequestBody OtpModel otpModel){
	return authService.verifyOTP(otpModel);
}
@PostMapping("/login")
public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest,HttpServletResponse response){
	return authService.login(authRequest,response);
}




}
