package com.sloth.OnlyStudent.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
	
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
@NoArgsConstructor
public class User{
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String login;
	private String password;
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
	
	public User() {
		
	}
    
	public User(Long id, String name, String login, String password, UserRole role) {
		this.id = id;
		this.name = name;
		this.login = login;
		this.password = password;
		this.role = role;
	}
	
	public UserRole getRoles() {
		return role;
	}
	public void setRoles(UserRole role) {
		this.role = role;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
