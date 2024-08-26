package com.sloth.OnlyStudent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Classroom;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

	List<Classroom> findTop3ByEducatorIdOrderByCodigoAsc(Long codigo);
	
	Page<Classroom> findByEducatorId(Long codigo, Pageable pageable);
	
	@Query(value = "SELECT c.* FROM classroom c JOIN classroom_student cs ON c.codigo = cs.classroom_id WHERE cs.student_id = :studentId",
	           countQuery = "SELECT count(c.id) FROM classroom_student cs WHERE cs.student_id = :studentId",
	           nativeQuery = true)
	    Page<Classroom> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

	Optional<Classroom> findByCodigo(Long código);
	
	List<Classroom> findTop3ByAlunosIdOrderByCodigoAsc(Long studentId);
	
	boolean existsByName(String name);

	List<Classroom> findByNameContainingIgnoreCase(String name);
	
	Page<Classroom> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
}
