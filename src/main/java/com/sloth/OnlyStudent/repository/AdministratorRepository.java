package com.sloth.OnlyStudent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sloth.OnlyStudent.entities.Administrator;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long>{

}
