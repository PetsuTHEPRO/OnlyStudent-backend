package com.sloth.OnlyStudent.entities.DTO;

import com.sloth.OnlyStudent.entities.MaterialType;

public record MaterialDetailsDTO(Long id, String name, MaterialType tipo, String url) {

}
