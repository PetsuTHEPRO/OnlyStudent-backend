package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.NotNull;

public record StudentRequestDTO(@NotNull(message = "O identificador do aluno é obrigatório.") Long idAluno) {
}
