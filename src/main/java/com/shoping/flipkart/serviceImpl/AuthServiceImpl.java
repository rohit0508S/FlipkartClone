package com.shoping.flipkart.serviceImpl;


import java.util.Date;
import java.util.Random;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.shoping.flipkart.Repository.CustomerRepo;
import com.shoping.flipkart.Repository.SellerRepo;
import com.shoping.flipkart.Repository.UserRepo;
import com.shoping.flipkart.cacahe.CacheStore;
import com.shoping.flipkart.entity.Customer;
import com.shoping.flipkart.entity.Seller;
import com.shoping.flipkart.entity.User;
import com.shoping.flipkart.request.OtpModel;
import com.shoping.flipkart.request.UserRequest;
import com.shoping.flipkart.response.UserResponse;
import com.shoping.flipkart.service.AuthService;
import com.shoping.flipkart.utility.MessageStructure;
import com.shoping.flipkart.utility.ResponseStructure;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{
	private JavaMailSender javaMailSender;
	private SellerRepo sellerRepo;
	private CustomerRepo customerRepo;
	private UserRepo userRepo;
	private ResponseStructure<UserResponse> structure;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	
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

		if(userRepo.existsByEmail(userRequest.getEmail()))
	 	    throw new RuntimeException("User is already present with the given email id");
		
		String OTP=generateOTP();
		
		User user=mapToUserRequest(userRequest);		
		UserResponse userResponse=mapToUserResponse(user);
		
		userCacheStore.add(userRequest.getEmail(), user);
		otpCacheStore.add(userRequest.getEmail(), OTP);		
		
		try {
			sendOtpToMail(user, OTP);
		} catch (MessagingException e) {
		
			log.error("The email address does't exist");
		}
		
		try {
			sendConfirmationMessage(user);
		} catch (MessagingException e) {
			log.error("Congrats your registration completed successfully!");
		}
		
		
	    structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("User registerd Successfully "+OTP);
		structure.setData(userResponse);
		
		return new  ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<String> verifyOTP(OtpModel otpModel) {
		User user=userCacheStore.get(otpModel.getEmail());
		String otp=otpCacheStore.get(otpModel.getEmail());
		
		if(otp==null) throw new RuntimeException("OTP expired");
		if(user==null) throw new RuntimeException("Registration session expired");
		if(!otp.equals(otpModel.getOtp())) throw new RuntimeException("invalid otp");
		
		user.setEmailVerified(true);
		userRepo.save(user);
		return new ResponseEntity<String>("Registration done successfully ",HttpStatus.CREATED);		
	}
	
	private void sendOtpToMail(User user,String otp) throws MessagingException {
	
		sendMail( MessageStructure.builder()
				.to(user.getEmail())
				.subject("Complete your registration to flipkart")
				.sentDate(new Date())
				.text(
						"hey, "+user.getUsername()
						+"Good to see you are interested in flipkart "
						+" Complete your registration using the OTP <br>"
						+"<h1>"+otp+"</h1><br>"
						+"Note: the OTP expire in 1 minute"
						+"<br><br>"
						+"with best regards<br>"
						+"Flipkart"
						).build() );		
	}
	
	private void sendConfirmationMessage(User user) throws MessagingException {
	    sendMail(MessageStructure.builder()
	            .to(user.getEmail())
	            .subject("Registration Confirmation - Flipkart")
	            .sentDate(new Date())
	            .text(
	                    "Dear " + user.getUsername() + ",\n\n"
	                            + "Thank you for registering with Flipkart! Your registration is now confirmed.\n\n"
	                            + "You can now enjoy shopping on Flipkart with your registered account.\n\n"
	                            + "Best regards,\n"
	                            + "Flipkart Team"
	            ).build());
	}

	
	
	
	
	@Async
	private void sendMail(MessageStructure message) throws MessagingException{
		MimeMessage mimeMessage=javaMailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage,true);
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText(),true);
		javaMailSender.send(mimeMessage);
	}
	
	
	private  String generateOTP() {
	    return String.valueOf(new Random().nextInt(100000,999999));
	}
	
		
}
