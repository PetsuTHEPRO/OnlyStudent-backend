package com.sloth.OnlyStudent.entities;

import jakarta.persistence.Entity;
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
	
	public Student() {
		
	}
	
	public Student(Long id, String name, String login, String password, UserRole roles) {
		super(id, name, login, password, roles);
		// TODO Auto-generated constructor stub
	}

}
