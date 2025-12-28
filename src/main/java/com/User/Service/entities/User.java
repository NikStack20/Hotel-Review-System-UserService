package com.User.Service.entities;
import java.util.ArrayList;       
import java.util.List;
import com.User.Service.loadouts.RatingDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
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
	private List<RatingDto> ratings = new ArrayList<>();
	// other entities

}
