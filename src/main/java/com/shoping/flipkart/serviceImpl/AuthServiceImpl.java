package com.shoping.flipkart.serviceImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.shoping.flipkart.Repository.CustomerRepo;
import com.shoping.flipkart.Repository.SellerRepo;
import com.shoping.flipkart.Repository.UserRepo;
import com.shoping.flipkart.entity.Customer;
import com.shoping.flipkart.entity.Seller;
import com.shoping.flipkart.entity.User;
import com.shoping.flipkart.request.UserRequest;
import com.shoping.flipkart.response.UserResponse;
import com.shoping.flipkart.service.AuthService;
import com.shoping.flipkart.utility.ResponseStructure;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{
	
	private SellerRepo sellerRepo;
	private CustomerRepo customerRepo;
	private UserRepo userRepo;
	private ResponseStructure<UserResponse> structure;
	
	public <T extends User>T mapToUserRequest(UserRequest userRequest){		
		User user=null;
		
		switch(userRequest.getUserRole()) {		
		case CUSTOMER->{
			user=new Customer();
		}
		case SELLER ->{
			user =new Seller();
		}	
		default -> throw new RuntimeException();
		}
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
		user.setUsername(userRequest.getEmail().split("@")[0]);
		user.setUserRole(userRequest.getUserRole());	
		
		return (T)user;		
	}
	
	
	
	public UserResponse mapToUserResponse(User user) {
		return new UserResponse().builder()
				.userId(user.getUserId())
				.email(user.getEmail())
				.username(user.getUsername())
				.isDeleted(user.isDeleted())
				.isEmailVerified(user.isEmailVerified())
				.build();
	}
	
	private User saveUser(UserRequest userRequest) {
	User user=mapToUserRequest(userRequest);
		switch(userRequest.getUserRole()) {		
		case CUSTOMER->{
			user=customerRepo.save((Customer)user);
		}
		case SELLER ->{
			user =sellerRepo.save((Seller)user);
		}	
		default -> throw new RuntimeException();
		}
		return user;
	}
	
	
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		
	    User user=userRepo.findByUsername(userRequest.getEmail().split("@")[0]).map(u->{
	    	if(u.isEmailVerified())
	    		throw new RuntimeException("User is already verified !");
	    	else
	    	{
	    		//send an email to the client with otp
	    	}
	    	return u;
	    }).orElse(saveUser(userRequest));	   
	 	    
		UserResponse userResponse=mapToUserResponse(user);   
	    structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("User registerd Successfully");
		structure.setData(userResponse);
		
		return new  ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.CREATED);
	}
		
}
