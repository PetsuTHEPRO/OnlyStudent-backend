package com.sloth.OnlyStudent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sloth.OnlyStudent.entities.Student;

public interface StudentRepository extends JpaRepository<Student, Long>{

}
