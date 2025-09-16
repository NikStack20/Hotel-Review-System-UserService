package com.User.Service.Configurations;

import org.modelmapper.ModelMapper; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class UserConfig {

	@Bean
	ModelMapper modelMapper() {
		return new ModelMapper();
	}

}
