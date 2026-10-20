package com.sloth.OnlyStudent.application.profile;

import org.springframework.stereotype.Component;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.DTO.EducatorResponseDTO;

@Component
public class EducatorProfileMapper {

    public EducatorResponseDTO toResponse(Educator educator) {
        return new EducatorResponseDTO(educator.getName(), educator.getTelephone(), educator.getEmail(),
                educator.getEspecialidade());
    }
}
