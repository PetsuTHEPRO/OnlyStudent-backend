package com.sloth.OnlyStudent.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "student")
public class Student extends User{
	
    @ManyToMany(mappedBy = "alunos")
    private Set<Classroom> classrooms = new HashSet<>();

	protected Student() {
		super();
	}
	
	public Student(Long id, String name, String telephone, String login, String password, UserRole roles) {
		super(id, name, telephone, login, password, roles);
	
	}

	public Set<Classroom> getClassrooms() {
		return classrooms;
	}

	public void setClassrooms(Set<Classroom> classrooms) {
		this.classrooms = classrooms;
	}

}
