package com.User.Service.servicesImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.User.Service.loadouts.HotelDto;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class HotelServiceClient {

	@Autowired
	private HotelClient hotelClient;

	private Logger logger = org.slf4j.LoggerFactory.getLogger(HotelServiceClient.class);

	@Retry(name = "userHotelService", fallbackMethod = "userHotelFallback")
	@RateLimiter(name = "userRateLimiter", fallbackMethod = "userHotelFallback")
	public Map<String, HotelDto> fetchHotelsForIds(Set<String> hotelIds) {

		if (hotelIds == null || hotelIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, HotelDto> hotelMap = new HashMap<>();

		for (String hotelId : hotelIds) {
			try {
				HotelDto hotel = hotelClient.getHotel(hotelId);
				hotelMap.put(hotelId, hotel);
			} catch (Exception e) {
				logger.warn("Hotel fetch failed for hotelId {} : {}", hotelId, e.getMessage());
			}
		}

		return hotelMap;
	}

	public Map<String, HotelDto> userHotelFallback(Set<String> hotelIds, Throwable ex) {
		logger.error("Fallback triggered for hotel service: {}", ex.getMessage());
		return Collections.emptyMap();
	}
}
