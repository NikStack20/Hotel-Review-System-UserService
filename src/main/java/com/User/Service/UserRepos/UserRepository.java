package com.User.Service.UserRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.User.Service.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	// Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	// using String BEcz of performance
}
