package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SupportDTO(
        @NotBlank(message = "Nome é obrigatório.")
        String name,
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email,
        @NotBlank(message = "Telefone é obrigatório.")
        String phone,
        @NotBlank(message = "Motivação é obrigatória.")
        String razao,
        @NotBlank(message = "Descrição é obrigatória.")
        String descricao
) {
}
