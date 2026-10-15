package com.sloth.OnlyStudent.application.administrator;

import org.springframework.stereotype.Component;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;

@Component
public class AdministratorResponseMapper {

    public AdministratorResponseDTO toResponse(Administrator administrator) {
        return new AdministratorResponseDTO(administrator.getId(), administrator.getName(),
                administrator.getTelephone(), administrator.getEmail(), administrator.getRoles(),
                administrator.getRoles());
    }
}
