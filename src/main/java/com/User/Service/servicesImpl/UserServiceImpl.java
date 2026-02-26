package com.User.Service.servicesImpl;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.User.Service.GlobalExceptionHandler.ConflictHandler;
import com.User.Service.GlobalExceptionHandler.DBExceptions;
import com.User.Service.UserRepos.UserRepository;
import com.User.Service.entities.User;
import com.User.Service.loadouts.HotelDto;
import com.User.Service.loadouts.RatingDto;
import com.User.Service.loadouts.UserDto;
import com.User.Service.services.UserService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	@Qualifier("hotelWebClient")
	private WebClient hotelWebClient;

	@Autowired
	@Qualifier("ratingWebClient")
	private WebClient ratingWebClient; // Web-Client Bean Injection

	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public UserDto saveUser(UserDto userDto) {

		User user = this.modelMapper.map(userDto, User.class);

		if (userRepo.existsByEmail(user.getEmail())) {
			throw new ConflictHandler("Email already registered: " + user.getEmail());
		}
		// 3) Generate ID if missing
		if (user.getUserId() == null || user.getUserId().isBlank()) {
			user.setUserId(UUID.randomUUID().toString());
		}

		user.setName(user.getName());
		user.setEmail(user.getEmail());
		user.setAbout(user.getAbout());
		User saved = this.userRepo.save(user);
		return this.modelMapper.map(saved, UserDto.class);

//		OR User savedUser=this.userRepo.save(user);
//		return savedUser;
	}

	@Override
	public List<UserDto> getAllUsers() {

		List<User> allUser = this.userRepo.findAll();

		List<UserDto> userDtos = allUser.stream().map(user -> this.modelMapper.map(user, UserDto.class))
				.collect(Collectors.toList());
		return userDtos;
	}

	@Override
	public UserDto getUser(String userId) {

		// 1️⃣ Fetch user from DB
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new DBExceptions("User with given userId: " + userId + " not found"));

		// 1st SERVICE CALL to RATING SERVICE
		List<RatingDto> ratings = ratingWebClient.get().uri("/ratings/getAllByUserId/{userId}", userId).retrieve()
				.onStatus(HttpStatusCode::is4xxClientError,
						resp -> resp.bodyToMono(String.class)
								.map(msg -> new DBExceptions("Rating service 4xx error: " + msg)))
				.onStatus(HttpStatusCode::is5xxServerError,
						resp -> resp.bodyToMono(String.class)
								.map(msg -> new DBExceptions("Rating service 5xx error: " + msg)))
				.bodyToFlux(RatingDto.class).timeout(Duration.ofSeconds(3)).collectList()
				.doOnError(e -> logger.error("Error while calling Rating-Service", e)).block();

		if (ratings == null || ratings.isEmpty()) {
			ratings = Collections.emptyList();
		}

		logger.info("Ratings fetched for user {} : {}", userId, ratings.size());

		// 3️⃣ Extract hotelIds from ratings
		Set<String> hotelIds = ratings.stream().map(RatingDto::getHotelId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		// 4️⃣ Fetch hotels from HOTEL-SERVICE (batch)
		Map<String, HotelDto> hotelMap = fetchHotelsForIds(hotelIds);

		// 5️⃣ Attach hotel data to each rating
		ratings.forEach(rating -> rating.setHotel(hotelMap.get(rating.getHotelId())));

		// 6️⃣ Build final UserDto response
		UserDto userDto = modelMapper.map(user, UserDto.class);
		userDto.setRatings(ratings);

		return userDto;
	}

	// 2nd SERVICE CALL to HOTEL SERVICE =======
	private Map<String, HotelDto> fetchHotelsForIds(Set<String> hotelIds) {

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

	@Override
	public UserDto updateUser(UserDto userDto, String userId) {
		User user = this.modelMapper.map(userDto, User.class);
		User hashuser = this.userRepo.findById(userId)
				.orElseThrow(() -> new DBExceptions("User with given userId:" + userId + ", not Found on server x_X"));
		hashuser.setName(user.getName());
		hashuser.setEmail(user.getEmail());
		hashuser.setAbout(user.getAbout());

		User updated = this.userRepo.save(hashuser);

		return this.modelMapper.map(updated, UserDto.class);
	}

	@Override
	public void deleteUser(String userId) {

		User user = this.userRepo.findById(userId)
				.orElseThrow(() -> new DBExceptions("User with given userId:" + userId + ", not Found on server x_X"));
		this.userRepo.delete(user);
	}

//	public boolean findByEmail(String email) {
//		Optional<User> user = this.userRepo.findByEmail(email);
//		return user.isPresent();
//
//	}

}
