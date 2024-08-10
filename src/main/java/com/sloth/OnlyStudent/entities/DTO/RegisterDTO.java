package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.UserRole;

public record RegisterDTO(String email, String telephone, String name, String password, UserRole role, String especialidade) {

}
