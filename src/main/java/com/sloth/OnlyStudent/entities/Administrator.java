package com.sloth.OnlyStudent.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "administrator")
public class Administrator extends User{

	protected Administrator() {
		super();
	}
	
	public Administrator(Long id, String name, String telephone, String login, String password, UserRole role) {
		super(id, name, telephone, login, password, role);
	}
    
}
