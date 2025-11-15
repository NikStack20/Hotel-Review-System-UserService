package com.User.Service.Configurations;

import org.modelmapper.ModelMapper;   
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;
 
@Data
@Configuration
public class UserConfig {

	@Bean
	ModelMapper modelMapper() {
		return new ModelMapper();
	}
	@Configuration
	public class WebClientConfig {

	    @Bean
	     WebClient webClient(WebClient.Builder builder) {
	        return builder
	                .baseUrl("http://localhost:7060")   // your rating service base
	                .build();
	    }
	}


}
