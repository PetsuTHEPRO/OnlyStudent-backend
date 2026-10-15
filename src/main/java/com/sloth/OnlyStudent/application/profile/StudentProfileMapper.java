package com.sloth.OnlyStudent.application.profile;

import org.springframework.stereotype.Component;

import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.DTO.StudentResponseDTO;

@Component
public class StudentProfileMapper {

    public StudentResponseDTO toResponse(Student student) {
        return new StudentResponseDTO(student.getName(), student.getTelephone(), student.getEmail());
    }
}
