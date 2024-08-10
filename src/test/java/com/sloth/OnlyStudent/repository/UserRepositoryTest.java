package com.sloth.OnlyStudent.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.UserDTO;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
	
	@Autowired
    private UserRepository repository;

	@Test
	@DisplayName("Should get User sucessfully from BD")
	void findUserBy() {
		String login = "admin1";
		UserDTO data = new UserDTO(1L, "yuri", "9898-9898", "admin1", "1234", UserRole.ADMIN);
		//this.createUser(data);
		
		User result = this.repository.findByEmail(login);
	}
	
	private void createUser(UserDTO data) {
		User newUser = new Administrator(data.id(), data.name(), data.telephone(), data.email(), data.password(), data.role());
		User user = this.repository.findByEmail(data.email());
		
	}
}
