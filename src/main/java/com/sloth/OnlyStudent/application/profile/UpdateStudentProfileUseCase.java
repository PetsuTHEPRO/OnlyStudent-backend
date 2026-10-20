package com.sloth.OnlyStudent.application.profile;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentResponseDTO;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

@Service
public class UpdateStudentProfileUseCase {

    private final StudentRepository studentRepository;
    private final StudentProfileMapper mapper;

    public UpdateStudentProfileUseCase(StudentRepository studentRepository, StudentProfileMapper mapper) {
        this.studentRepository = studentRepository;
        this.mapper = mapper;
    }

    @Transactional
    public Optional<StudentResponseDTO> execute(Long principalId, String requestedLogin, EducatorDTO request) {
        Optional<Student> optionalStudent = studentRepository.findById(principalId);
        if (optionalStudent.isEmpty()) {
            return Optional.empty();
        }

        Student student = optionalStudent.get();
        if (requestedLogin != null && !requestedLogin.equals(student.getEmail())) {
            throw new ProfileAccessDeniedException();
        }
        student.setName(request.name());
        student.setTelephone(request.telefone());
        return Optional.of(mapper.toResponse(studentRepository.save(student)));
    }
}
