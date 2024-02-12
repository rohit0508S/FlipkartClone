package com.shoping.flipkart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.CookieValue;
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
import com.shoping.flipkart.utility.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@EnableMethodSecurity
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
@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")
@PostMapping("/login")
public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest,HttpServletResponse response){
	return authService.login(authRequest,response);
}

@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")
@PostMapping("/logout")
public ResponseEntity<SimpleResponseStructure<AuthResponse>> logout(@CookieValue(name="rt",required = false) String refreshToken ,@CookieValue(name="at" ,required=true)String accessToken,HttpServletResponse response){
	return authService.logout(refreshToken,accessToken,response);
}
@PostMapping("/revoke-access")
public ResponseEntity<SimpleResponseStructure<AuthResponse>> revokeOther(String accessToken,String refreshToken,HttpServletResponse response){
	return revokeOther(accessToken,refreshToken,response);
}



}
