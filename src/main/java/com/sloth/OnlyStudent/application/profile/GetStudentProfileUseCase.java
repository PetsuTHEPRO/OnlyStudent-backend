package com.sloth.OnlyStudent.application.profile;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.DTO.StudentResponseDTO;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

@Service
public class GetStudentProfileUseCase {

    private final StudentRepository studentRepository;
    private final StudentProfileMapper mapper;

    public GetStudentProfileUseCase(StudentRepository studentRepository, StudentProfileMapper mapper) {
        this.studentRepository = studentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Optional<StudentResponseDTO> execute(Long principalId, Long requestedId) {
        if (requestedId != null && !Objects.equals(requestedId, principalId)) {
            throw new ProfileAccessDeniedException();
        }

        return studentRepository.findById(principalId).map(mapper::toResponse);
    }
}
