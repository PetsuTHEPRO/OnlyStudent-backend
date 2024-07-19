package com.sloth.OnlyStudent.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByLogin(String login);
}
