package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ClassroomDTO(
        @NotBlank(message = "O nome da turma é obrigatório.")
        @Size(max = 100, message = "O nome da turma deve ter no máximo 100 caracteres.")
        String name,
        String description,
        Long idEducator,
        @PositiveOrZero(message = "O preço da turma não pode ser negativo.")
        double price) {
}
