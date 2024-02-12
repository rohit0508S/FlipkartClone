package com.shoping.flipkart.utility;

import org.springframework.stereotype.Component;

@Component
public class SimpleResponseStructure<T> {
	private int status;
	private String message;
	public int getStatus() {
		return status;
	}
	public SimpleResponseStructure<T> setStatus(int status) {
		this.status = status;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public SimpleResponseStructure<T> setMessage(String message) {
		this.message = message;
		return this;
	}
	
}
