package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.UserRole;

public record AdministratorResponseDTO(
        Long id,
        String name,
        String telephone,
        String email,
        UserRole role,
        UserRole roles) {
}
