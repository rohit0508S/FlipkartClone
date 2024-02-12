package com.shoping.flipkart.service;

import org.springframework.http.ResponseEntity;

import com.shoping.flipkart.request.AuthRequest;
import com.shoping.flipkart.request.OtpModel;
import com.shoping.flipkart.request.UserRequest;
import com.shoping.flipkart.response.AuthResponse;
import com.shoping.flipkart.response.UserResponse;
import com.shoping.flipkart.utility.ResponseStructure;
import com.shoping.flipkart.utility.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest);

	ResponseEntity<String> verifyOTP(OtpModel otpModel);

	ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response);

//	ResponseEntity<ResponseStructure<AuthResponse>> logout(HttpServletRequest request, HttpServletResponse response);

	ResponseEntity<SimpleResponseStructure<AuthResponse>> logout(String refreshToken, String accessToken,
			HttpServletResponse response);

}
