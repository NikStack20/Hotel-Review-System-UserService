package com.User.Service.servicesImpl;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.User.Service.entities.User;
import com.User.Service.loadouts.HotelDto;
import com.User.Service.loadouts.UserDto;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class HotelServiceClient {

	@Autowired
	@Qualifier("hotelWebClient")
	private WebClient hotelWebClient;

	@Autowired
	private ModelMapper modelMapper;

	private Logger logger = org.slf4j.LoggerFactory.getLogger(HotelServiceClient.class);

	// Applied Retry+Rate-limiter for hotelService Protection

	@Retry(name = "ratingHotelService", fallbackMethod = "ratingHotelFallback")
	@RateLimiter(name = "userRateLimiter", fallbackMethod = "ratingHotelFallback")
	public Map<String, HotelDto> fetchHotelsForIds(Set<String> hotelIds) {

		if (hotelIds == null || hotelIds.isEmpty()) {
			return Collections.emptyMap();
		}

		final int CONCURRENCY = 10;

		try {
			List<HotelDto> hotels = Flux.fromIterable(hotelIds)
					.flatMap(hotelId -> hotelWebClient.get().uri("/hotels/getHotel/{hotelId}", hotelId)
							.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(HotelDto.class)
							.timeout(Duration.ofSeconds(3)).onErrorResume(e -> {
								logger.warn("Hotel fetch failed for hotelId {} : {}", hotelId, e.getMessage());
								return Mono.empty();
							}), CONCURRENCY)
					.collectList().block();

			if (hotels == null || hotels.isEmpty()) {
				return Collections.emptyMap();
			}

			return hotels.stream().filter(h -> h.getHotelId() != null)
					.collect(Collectors.toMap(HotelDto::getHotelId, Function.identity()));

		} catch (Exception ex) {
			logger.error("Failed to fetch hotels", ex);
			return Collections.emptyMap();
		}
	}

	// Fallback for ratingHotelBreaker
	public UserDto ratingHotelFallback(String userId, Exception ex) {
//		logger.info("Fallback is executed because service is down : ", ex.getMessage());

		User user = User.builder().email(" xyz123@gmail.com ").name(" John Doe ")
				.about("User field is shown with dummy fileds because some services are Down").userId("1234John")
				.build();
		return modelMapper.map(user, UserDto.class);
	}

}
