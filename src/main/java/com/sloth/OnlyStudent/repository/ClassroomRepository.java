package com.sloth.OnlyStudent.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Classroom;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

	List<Classroom> findTop3ByEducatorIdOrderByCodigoAsc(Long codigo);
	
	Page<Classroom> findByEducatorId(Long codigo, Pageable pageable);
	
	boolean existsByName(String name);
}
