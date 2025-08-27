package com.User.Service.services;

import java.util.List;

import com.User.Service.entities.User;

public interface UserService {

	// Creating User
	User saveUser(User user);

	// Get All USers
	List<User> getAllUsers();

	// Get SIngle User with Id
	User getUser(String userId);

	// Update User
	User updateUser(User user, String userId);

	// Delete User
	void deleteUser(String userId);
}
