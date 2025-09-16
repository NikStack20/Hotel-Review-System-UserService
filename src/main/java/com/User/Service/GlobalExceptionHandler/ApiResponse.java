package com.User.Service.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;  
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ApiResponse {

	private String message;
	private boolean success;
	private HttpStatus status;

}
