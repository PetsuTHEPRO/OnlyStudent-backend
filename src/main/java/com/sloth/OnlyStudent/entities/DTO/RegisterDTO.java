package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.UserRole;

public record RegisterDTO(String login, String name, String password, UserRole role) {

}
