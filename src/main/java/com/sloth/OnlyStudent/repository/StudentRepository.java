package com.sloth.OnlyStudent.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>{

	Optional<Student> findById(Long id);

	Student findByEmail(String login);

}
