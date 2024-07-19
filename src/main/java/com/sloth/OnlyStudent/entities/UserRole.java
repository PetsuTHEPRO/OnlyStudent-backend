package com.sloth.OnlyStudent.entities;

public enum UserRole {

	ADMIN("admin"),
	EDUCATOR("educator"),
	STUDENT("student");
	
	private String role;
	
	UserRole(String role) {
		this.role = role;
	}
	
	public String getRole() {
		return this.role;
	}
}
