package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.MaterialType;

public record MaterialResponseDTO(Long id, MaterialType tipo, String name, String url) {
}
