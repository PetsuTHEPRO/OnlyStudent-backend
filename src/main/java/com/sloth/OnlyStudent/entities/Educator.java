package com.sloth.OnlyStudent.entities;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "educator")
@Getter
@Setter
@NoArgsConstructor
public class Educator extends User{

	public Educator() {
		
	}
	
	public Educator(Long id, String name, String login, String password, UserRole role) {
		super(id, name, login, password, role);
		// TODO Auto-generated constructor stub
	}

}
