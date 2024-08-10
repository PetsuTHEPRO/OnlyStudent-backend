package com.sloth.OnlyStudent.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Educator;

@Repository
public interface EducatorRepository extends JpaRepository<Educator, Long>{

	Optional<Educator> findById(Long id);
	
	// Método para encontrar o Educator pelo nome
	Educator findByEmail(String email);
}
