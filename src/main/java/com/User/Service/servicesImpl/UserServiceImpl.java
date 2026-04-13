package com.User.Service.servicesImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.User.Service.GlobalExceptionHandler.ConflictHandler;
import com.User.Service.GlobalExceptionHandler.DBExceptions;
import com.User.Service.UserRepos.UserRepository;
import com.User.Service.entities.User;
import com.User.Service.loadouts.HotelDto;
import com.User.Service.loadouts.RatingDto;
import com.User.Service.loadouts.UserDto;
import com.User.Service.services.UserService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private HotelServiceClient hotelServiceClient;

	@Autowired
	private RatingClient ratingClient; // Web-Client Bean Injection

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

	// getUser
	// Configuring resilence4j for this controller with fallbackMethod

	// Initialising retry
	int retryCount = 1;

	@Override
	@Retry(name = "ratingHotelService", fallbackMethod = "ratingHotelFallback")
	@CircuitBreaker(name = "ratingHotelBreaker", fallbackMethod = "ratingHotelFallback")
	public UserDto getUser(String userId) {

		logger.info("Retry count: {}", retryCount);
		retryCount++;

		// Fetch user from DB
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new DBExceptions("User with given userId: " + userId + " not found"));

		// 1st SERVICE CALL to RATING SERVICE
		List<RatingDto> ratings;
		try {
			ratings = ratingClient.getRatings(userId);
		} catch (Exception e) {
			logger.error("Error while calling Rating-Service", e);
			ratings = Collections.emptyList();
		}

		if (ratings == null || ratings.isEmpty()) {
			ratings = Collections.emptyList();
		}

		logger.info("Ratings fetched for user {} : {}", userId, ratings.size());

		// 2nd SERVICE CALL to HOTEL SERVICE
		// Extract hotelIds from ratings and Fetch hotels from HOTEL-SERVICE (batch)
		Set<String> hotelIds = ratings.stream().map(RatingDto::getHotelId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<String, HotelDto> hotelMap = hotelServiceClient.fetchHotelsForIds(hotelIds);

		ratings.forEach(rating -> rating.setHotel(hotelMap.get(rating.getHotelId())));

		UserDto userDto = modelMapper.map(user, UserDto.class);
		userDto.setRatings(ratings);

		return userDto;
	}

	public UserDto ratingHotelFallback(String userId, Throwable ex) {

		logger.error("Fallback triggered in UserService: {}", ex.getMessage());

		User user = User.builder().email("xyz123@gmail.com").name("John Doe")
				.about("Some services are down, fallback response").userId("fallback-id").build();

		UserDto userDto = modelMapper.map(user, UserDto.class);

		userDto.setRatings(Collections.emptyList());

		return userDto;
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

	/*
	 * public boolean findByEmail(String email) { Optional<User> user =
	 * this.userRepo.findByEmail(email); return user.isPresent();
	 * 
	 * }
	 */

}
