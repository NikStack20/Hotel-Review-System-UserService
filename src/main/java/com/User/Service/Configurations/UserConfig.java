package com.User.Service.Configurations;
import org.modelmapper.ModelMapper;  
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
 
@Configuration
public class UserConfig {

	@Bean
	ModelMapper modelMapper() {
		return new ModelMapper();
	}
	@Configuration
	public class WebClientConfig {

		@Bean("hotelWebClient")
	     WebClient hotelWebClient(WebClient.Builder builder) {
	        return builder
	                .baseUrl("http://HOTEL-SERVICE") // HOTEL service
	                .build();
	    }

	    @Bean("ratingWebClient")
	     WebClient ratingWebClient(WebClient.Builder builder) {
	        return builder
	                .baseUrl("http://RATING-SERVICE") // RATING service (if needed)
	                .build();
	    }
	}


}
