package com.sloth.OnlyStudent.application.material;

import org.springframework.stereotype.Component;

import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.DTO.MaterialResponseDTO;

@Component
public class MaterialResponseMapper {

    public MaterialResponseDTO toResponse(Material material) {
        return new MaterialResponseDTO(material.getId(), material.getTipo(), material.getName(), material.getUrl());
    }
}
