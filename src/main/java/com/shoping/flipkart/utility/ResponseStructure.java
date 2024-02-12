package com.shoping.flipkart.utility;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component

public class ResponseStructure<T> {
		
		private int status;
		private String message;
		private T data;
		
		public int getStatus() {
			return status;
		}
		public ResponseStructure<T> setStatusCode(int status) {
			this.status = status;
			return this;
		}
		public String getMessage() {
			return message;
		}
		public ResponseStructure<T> setMessage(String message) {
			this.message = message;
			return this;
		}
		public T getData() {
			return data;
		}
		public ResponseStructure<T> setData(T data) {
			this.data = data;
			return this;
		}

	}

