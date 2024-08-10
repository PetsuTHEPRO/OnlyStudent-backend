package com.sloth.OnlyStudent.entities;

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
	
	private String especialidade;

	public Educator() {
		
	}
	
	public Educator(Long id, String name, String telephone, String login, String password, UserRole role, String especialidade) {
		super(id, name, telephone, login, password, role);
		this.especialidade = especialidade;
	}

	public String getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}

}
