package com.User.Service.Configurations;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LoadBalancedWebClientConfig {

	@Bean
	@LoadBalanced
	WebClient.Builder webClientBuilder() {
		return WebClient.builder().filter((request, next) -> {

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			if (auth instanceof JwtAuthenticationToken jwtAuth) {

				String token = jwtAuth.getToken().getTokenValue();

				ClientRequest newRequest = ClientRequest.from(request).header("Authorization", "Bearer " + token)
						.build();

				return next.exchange(newRequest);
			}

			return next.exchange(request);
		});
	}
}
