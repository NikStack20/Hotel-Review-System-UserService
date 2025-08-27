package com.User.Service.servicesImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.User.Service.GlobalExceptionHandler.DBExceptions;
import com.User.Service.UserRepos.UserRepository;
import com.User.Service.entities.User;
import com.User.Service.services.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Override
	public User saveUser(User user) {

		String randomUserId = UUID.randomUUID().toString(); // generating new unique id in String format
		user.setUserId(randomUserId);
		user.setName(user.getName());
		;
		user.setEmail(user.getEmail());
		user.setAbout(user.getAbout());
		return this.userRepo.save(user);

//		OR User savedUser=this.userRepo.save(user);
//		return savedUser;
	}

	@Override
	public List<User> getAllUsers() {

		return this.userRepo.findAll();
	}

	@Override
	public User getUser(String userId) {

		return this.userRepo.findById(userId)
				.orElseThrow(() -> new DBExceptions("User with given userId:" + userId + ", not Found on server x_x"));
	}

	@Override
	public User updateUser(User user, String userId) {
		User hashuser = this.userRepo.findById(userId)
				.orElseThrow(() -> new DBExceptions("User with given userId:" + userId + ", not Found on server x_x"));
		hashuser.setName(user.getName());
		hashuser.setEmail(user.getEmail());
		hashuser.setAbout(user.getAbout());

		return this.userRepo.save(hashuser);
	}

	@Override
	public void deleteUser(String userId) {
		this.userRepo.deleteById(userId);
		;

	}

}
