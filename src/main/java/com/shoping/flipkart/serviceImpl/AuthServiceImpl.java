package com.shoping.flipkart.serviceImpl;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shoping.flipkart.Repository.AccessTokenRepo;
import com.shoping.flipkart.Repository.CustomerRepo;
import com.shoping.flipkart.Repository.RefreshTokenRepo;
import com.shoping.flipkart.Repository.SellerRepo;
import com.shoping.flipkart.Repository.UserRepo;
import com.shoping.flipkart.cacahe.CacheStore;
import com.shoping.flipkart.entity.AccessToken;
import com.shoping.flipkart.entity.Customer;
import com.shoping.flipkart.entity.RefreshToken;
import com.shoping.flipkart.entity.Seller;
import com.shoping.flipkart.entity.User;
import com.shoping.flipkart.request.AuthRequest;
import com.shoping.flipkart.request.OtpModel;
import com.shoping.flipkart.request.UserRequest;
import com.shoping.flipkart.response.AuthResponse;
import com.shoping.flipkart.response.UserResponse;
import com.shoping.flipkart.security.JwtService;
import com.shoping.flipkart.service.AuthService;
import com.shoping.flipkart.utility.CookieManager;
import com.shoping.flipkart.utility.MessageStructure;
import com.shoping.flipkart.utility.ResponseStructure;
import com.shoping.flipkart.utility.SimpleResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service

public class AuthServiceImpl implements AuthService{
	private JavaMailSender javaMailSender;
	private SellerRepo sellerRepo;
	private CustomerRepo customerRepo;
	private UserRepo userRepo;
	private ResponseStructure<UserResponse> structure;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	private AuthenticationManager authenticationManager;
	private CookieManager cookieManager;
	private JwtService jwtService;
	private AccessTokenRepo accessTokenRepo;
	private RefreshTokenRepo refreshTokenRepo;
	private ResponseStructure<AuthResponse> authStructure;
	private SimpleResponseStructure<AuthResponse> simpleresponseStructure;



	@Value("${myapp.access.expiry}")
	private int accessExpiryInSeconds;

	@Value("${myapp.refresh.expiry}")
	private int refreshExpiryInSeconds;

	public AuthServiceImpl(JavaMailSender javaMailSender, SellerRepo sellerRepo, CustomerRepo customerRepo,
			UserRepo userRepo, ResponseStructure<UserResponse> structure, CacheStore<String> otpCacheStore,
			CacheStore<User> userCacheStore, AuthenticationManager authenticationManager, CookieManager cookieManager,JwtService jwtService
			,AccessTokenRepo accessTokenRepo, RefreshTokenRepo refreshTokenRepo,SimpleResponseStructure<AuthResponse> responseStructure,
			ResponseStructure<AuthResponse> authStructure) {
		super();
		this.javaMailSender = javaMailSender;
		this.sellerRepo = sellerRepo;
		this.authStructure=authStructure;
		this.customerRepo = customerRepo;
		this.userRepo = userRepo;
		this.structure = structure;
		this.otpCacheStore = otpCacheStore;
		this.userCacheStore = userCacheStore;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService=jwtService;
		this.accessTokenRepo=accessTokenRepo;
		this.refreshTokenRepo=refreshTokenRepo;
		this.simpleresponseStructure=simpleresponseStructure;

	}




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
				
	
		userCacheStore.add(userRequest.getEmail(), user);
		otpCacheStore.add(userRequest.getEmail(), OTP);	
		System.out.println(user.getEmail());

		try {
			sendOtpToMail(user, OTP);
		} catch (MessagingException e) {

			log.error("The email address does't exist");
		}


		return new ResponseEntity<ResponseStructure<UserResponse>>(structure.setStatusCode(HttpStatus.OK.value())
				.setMessage("Please Varify your email by OTP sent to your email")
				.setData(mapToUserResponse(user)),HttpStatus.OK);
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
		
		try {
		sendConfirmationMessage(user);
	} catch (MessagingException e) {
		log.error("Congrats your registration completed successfully!");
	}
		
		
		
		return new ResponseEntity<String>("Registration done successfully ",HttpStatus.CREATED);		
	}


	private void grantAccess(HttpServletResponse response,User user) {
		String accessToken=jwtService.generateAccessToken(user.getUsername());
		String refreshToken=jwtService.generateRefreshToken(user.getUsername());
		//generating access and refresh token
		response.addCookie(cookieManager.configure(
				new Cookie("at", accessToken), accessExpiryInSeconds));

		//adding access and refresh token to the response
		response.addCookie(cookieManager.configure(
				new Cookie("rt", refreshToken), refreshExpiryInSeconds));


		//saving the access and refresh cookie in to the database
		accessTokenRepo.save(AccessToken.builder()
				.token(accessToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.build());
		refreshTokenRepo.save(RefreshToken.builder()
				.token(refreshToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
				.build());

	}



	private void sendOtpToMail(User user,String otp) throws MessagingException {

		System.out.println(user.getEmail()+"inside sendOtpToMail");
		
		
		 if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
		        log.error("User or user email is null or empty.");
		        throw new IllegalArgumentException("Invalid user or user email.");
		    }
		
		    log.info("Sending OTP to email: {}", user.getEmail());
		
		
		
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
		
		System.out.println(user.getEmail()+"inside send confirmation message");
		
		
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
		helper.setTo(message.getTo());
		javaMailSender.send(mimeMessage);
	}


	private  String generateOTP() {
		return String.valueOf(new Random().nextInt(100000,999999));
	}



	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response) {
		String username=authRequest.getEmail().split("@")[0];
		
		UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(authRequest.getPassword(),username);
		Authentication authentication=authenticationManager.authenticate(token);
		if(!authentication.isAuthenticated()) {
			throw new UsernameNotFoundException("Failed to authenticated the user");
		}
		else 
		
			//generating the cookies and authResponse and returning to the client
			
			return userRepo.findByUsername(username).map(user->{
				grantAccess(response,user);
				return ResponseEntity.ok(authStructure.setStatusCode(HttpStatus.OK.value()).setData(
						AuthResponse.builder()
						.userId(user.getUserId())
						.username(username)
						.role(user.getUserRole().name())
						.isAuthenticated(true)
						.accessExpiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
						.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
						.build())
						.setMessage("login successful ...!"));
			}).get();
	}




	
//	@Override
//	public ResponseEntity<ResponseStructure<AuthResponse>> logout(HttpServletRequest request,
//			HttpServletResponse response) {
//	String rt = null;
//	String at = null;
//	Cookie[]cookies=request.getCookies();
//	for(Cookie cookie:cookies) {
//		if(cookie.getName().equals("rt"))
//			rt=cookie.getValue();
//		if(cookie.getName().equals("at"))
//			at=cookie.getValue();
//		
//	}
//	accessTokenRepo.findByToken(at).ifPresent(accessToken->{
//		accessToken.setBlocked(true);
//		accessTokenRepo.save(accessToken);
//	});
//	refreshTokenRepo.findByToken(rt).ifPresent(refreshToken->{
//		refreshToken.setBlocked(true);
//		refreshTokenRepo.save(refreshToken);
//	});
//	
//	response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
//	response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
//	
//	authStructure.setStatusCode(HttpStatus.OK.value());
//	authStructure.setMessage("Logout successfull");
//	
//	
//	return new ResponseEntity<ResponseStructure<AuthResponse>>(authStructure,HttpStatus.OK);
//	}


	@Override
	public ResponseEntity<SimpleResponseStructure<AuthResponse>> logout(String refreshToken, String accessToken,
			HttpServletResponse response) {
		if(accessToken==null && refreshToken==null)
			throw new IllegalArgumentException("User not login !");
		
		accessTokenRepo.findByToken(accessToken).ifPresent(token->{
			token.setBlocked(true);
			accessTokenRepo.save(token);
		});
		
		refreshTokenRepo.findByToken(refreshToken).ifPresent(token->{
			token.setBlocked(true);
			refreshTokenRepo.save(token);
		});
		
		response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
		 SimpleResponseStructure<AuthResponse> authResponse = new SimpleResponseStructure<>();
		    authResponse.setStatus(HttpStatus.OK.value());
		    authResponse.setMessage("Logout successful");

		    return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	
	
	
	
	
	

	public void deleteExpiredTokens() {
		LocalDateTime currentTime=LocalDateTime.now();
		List<AccessToken> accessTokens = accessTokenRepo.findAllByExpirationBefore(currentTime);
		List<RefreshToken> refreshToken = refreshTokenRepo.findAllByExpirationBefore(currentTime);
		accessTokenRepo.deleteAll();
		refreshTokenRepo.deleteAll();
		
	}
	
	
	@Override
	public ResponseEntity<SimpleResponseStructure<AuthResponse>> revokeAllDevice(String accessToken,
			String refreshToken, HttpServletResponse response) {
		String user=SecurityContextHolder.getContext().getAuthentication().getName();
		if(user==null)
			throw new UsernameNotFoundException("User does't exists !");
		userRepo.findByUsername(user).ifPresent(user1->{
			blockAccessTokens(accessTokenRepo.findAllByUserAndIsBlockedAndTokenNot(user1,false,accessToken));
			blockRefreshTokens(refreshTokenRepo.findAllByUserAndIsBlockedAndTokenNot(user1,false,refreshToken));
		});
		
		
		 SimpleResponseStructure<AuthResponse> authResponse = new SimpleResponseStructure<>();
		    authResponse.setStatus(HttpStatus.OK.value());
		    authResponse.setMessage("Logout successful");

		    return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}
	
	
	
	private void blockAccessTokens(List<AccessToken> accessTokens) {
		accessTokens.forEach(at->{
			at.setBlocked(true);
			accessTokenRepo.save(at);
		});
	}
	private void blockRefreshTokens(List<RefreshToken> refreshTokens) {
		refreshTokens.forEach(rt->{
			rt.setBlocked(true);
			refreshTokenRepo.save(rt);
		});
	}




	

}
