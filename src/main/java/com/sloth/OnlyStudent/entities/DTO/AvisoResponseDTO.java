package com.sloth.OnlyStudent.entities.DTO;

import java.time.LocalDateTime;

public record AvisoResponseDTO(Long id, String mensagem, LocalDateTime dataCriacao) {
}