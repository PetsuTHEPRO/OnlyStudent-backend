package com.sloth.OnlyStudent.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "educator")
public class Educator extends User{
	
	private String especialidade;

	protected Educator() {
		super();
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
