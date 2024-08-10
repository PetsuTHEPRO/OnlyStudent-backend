package com.sloth.OnlyStudent.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrator")
@Getter
@Setter
@NoArgsConstructor
public class Administrator extends User{
	
	public Administrator() {
		
	}
	
	public Administrator(Long id, String name, String telephone, String login, String password, UserRole role) {
		super(id, name, telephone, login, password, role);
	}
    
}
