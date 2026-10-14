package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.application.auth.validation.ValidEducatorSpecialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@ValidEducatorSpecialty
public record RegisterDTO(
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.") String email,
        @NotBlank(message = "Telefone é obrigatório.") String telephone,
        @NotBlank(message = "Nome é obrigatório.") String name,
        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.") String password,
        @NotNull(message = "Perfil é obrigatório.") UserRole role,
        String especialidade) {

}
