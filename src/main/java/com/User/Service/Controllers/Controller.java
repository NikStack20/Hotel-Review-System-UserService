package com.User.Service.Controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.User.Service.entities.User;
import com.User.Service.loadouts.UserDto;
import com.User.Service.services.UserService;
import com.User.Service.servicesImpl.UserServiceImpl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class Controller {

	@Autowired
	private UserService userService;

	@Autowired
	private ModelMapper modelMapper;

	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	// create
	@PostMapping("/create")
	public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {

		return new ResponseEntity<UserDto>(this.userService.saveUser(userDto), HttpStatus.CREATED);
	}

	// update
	@PutMapping("/update/{userId}")
	public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto, @PathVariable String userId) {
		return new ResponseEntity<UserDto>(this.userService.updateUser(userDto, userId), HttpStatus.OK);
	}

	// getUser
	// Configuring resilence4j for this controller with fallbackMethod
	@GetMapping("/getUser/{userId}")
	@CircuitBreaker(name = "ratingHotelBreaker", fallbackMethod = "ratingHotelFallback")
	public ResponseEntity<UserDto> getUser(@PathVariable String userId) {

		return new ResponseEntity<UserDto>(this.userService.getUser(userId), HttpStatus.OK);
	}

	// Fallback for ratingHotelBreaker
	public ResponseEntity<UserDto> ratingHotelFallback(String userId, Exception ex) {
		logger.info("Fallback is executed because service is down : ", ex.getMessage());
		User user = User.builder().email(" xyz123@gmail.com ").name(" John Doe ")
				.about("User field is shown with dummy fileds because some services are Down").userId("1234John")
				.build();
		return new ResponseEntity<UserDto>(modelMapper.map(user, UserDto.class), HttpStatus.OK);
	}

	// getAllUser
	@GetMapping
	public ResponseEntity<List<UserDto>> getAllUser() {

		return new ResponseEntity<List<UserDto>>(this.userService.getAllUsers(), HttpStatus.OK);
	}

	// delete
	@DeleteMapping("/deleteUser/{userId}")
	void deleteUser(@PathVariable String userId) {
		this.userService.deleteUser(userId);
	}

}
