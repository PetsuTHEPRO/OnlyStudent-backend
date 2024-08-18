package com.sloth.OnlyStudent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>{

	List<Material> findByTurmaCodigo(Long codigo);
	
	boolean existsByNameAndTurmaCodigo(String name, Long turmaCodigo);
}
