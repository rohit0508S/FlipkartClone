package com.shoping.flipkart.response;

import com.shoping.flipkart.request.UserRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
	private int userId;
	private String username;
	private String email;
	private boolean isEmailVerified;
	private boolean isDeleted;
}
