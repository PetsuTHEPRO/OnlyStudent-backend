package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaterialDTO(
        @NotBlank(message = "O nome do material é obrigatório.")
        @Size(max = 255, message = "O nome do material deve ter no máximo 255 caracteres.")
        String name,
        @NotBlank(message = "O tipo do material é obrigatório.")
        String tipo,
        @NotBlank(message = "A URL do material é obrigatória.")
        String url,
        @NotNull(message = "O código da turma é obrigatório.")
        Long turmaCodigo) {
}
