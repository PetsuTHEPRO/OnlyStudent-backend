package com.sloth.OnlyStudent.application.auth.validation;

import org.springframework.util.StringUtils;

import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.entities.UserRole;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EducatorSpecialtyValidator implements ConstraintValidator<ValidEducatorSpecialty, RegisterDTO> {

    @Override
    public boolean isValid(RegisterDTO request, ConstraintValidatorContext context) {
        return request == null || request.role() != UserRole.EDUCATOR
                || StringUtils.hasText(request.especialidade());
    }
}
