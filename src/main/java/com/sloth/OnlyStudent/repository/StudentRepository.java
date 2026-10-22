package com.sloth.OnlyStudent.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>{

	@NonNull
	Optional<Student> findById(@NonNull Long id);

	Student findByEmail(String login);

}
