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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
	
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Setter
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User{
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String telephone;
	@Column(unique = true, nullable = false)
	private String email;
	private String password;
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
	
	public User() {
		
	}
    
	public User(Long id, String name, String telephone, String email, String password, UserRole role) {
		this.id = id;
		this.name = name;
		this.telephone = telephone;
		this.email = email;
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

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
