package com.sloth.OnlyStudent.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
public class Student extends User{
	
    @ManyToMany(mappedBy = "alunos")
    private Set<Classroom> classrooms = new HashSet<>();
	
	public Student(Long id, String name, String telephone, String login, String password, UserRole roles) {
		super(id, name, telephone, login, password, roles);
		// TODO Auto-generated constructor stub
	}

	public Set<Classroom> getClassrooms() {
		return classrooms;
	}

	public void setClassrooms(Set<Classroom> classrooms) {
		this.classrooms = classrooms;
	}

}
