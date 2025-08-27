package com.User.Service.Controllers;

import java.util.List;

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
import com.User.Service.services.UserService;

@RestController
@RequestMapping("/users")
public class Controller {

	@Autowired
	private UserService userService;

	// create
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody User user) {

		return new ResponseEntity<User>(this.userService.saveUser(user), HttpStatus.CREATED);
	}

	// update
	@PutMapping("/update/{userId}")
	public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable String userId) {

		return new ResponseEntity<User>(this.userService.updateUser(user, userId), HttpStatus.OK);
	}

	// update
	@GetMapping("/getUser/{userId}")
	public ResponseEntity<User> getUser(@PathVariable String userId) {

		return new ResponseEntity<User>(this.userService.getUser(userId), HttpStatus.OK);
	}

	// update
	@GetMapping("/")
	public ResponseEntity<List<User>> getAllUser() {

		return new ResponseEntity<List<User>>(this.userService.getAllUsers(), HttpStatus.OK);
	}

	// delete
	@DeleteMapping("/deleteUser/{userId}")
	void deleteUser(@PathVariable String userId) {

		this.userService.deleteUser(userId);
	}

}
