package com.User.Service.entities;
import java.util.ArrayList;  
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "micro_users")
public class User {

	@Id
	private String userId;

	@Column(name = "Names", nullable = false)
	private String name;

	@Column(name = "EMAILS", nullable = false)
	private String email;

	@Column(name = "ABOUT", nullable = false)
	private String about;

	@Transient // Database X
	private List<Rating> ratings = new ArrayList<>();
	// other entities

}
