package com.sloth.OnlyStudent.application.classroom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNameAlreadyExistsException;
import com.sloth.OnlyStudent.shared.errors.EducatorNotFoundException;

@Service
public class CreateClassroomUseCase {

    private final ClassroomRepository classroomRepository;
    private final EducatorRepository educatorRepository;

    public CreateClassroomUseCase(ClassroomRepository classroomRepository, EducatorRepository educatorRepository) {
        this.classroomRepository = classroomRepository;
        this.educatorRepository = educatorRepository;
    }

    @Transactional
    public Classroom execute(ClassroomDTO request, Long principalId, UserRole principalRole) {
        if (principalRole != UserRole.EDUCATOR) {
            throw new ClassroomAccessDeniedException();
        }
        if (classroomRepository.existsByName(request.name())) {
            throw new ClassroomNameAlreadyExistsException();
        }
        Educator educator = educatorRepository.findById(principalId)
                .orElseThrow(EducatorNotFoundException::new);
        return classroomRepository.save(Classroom.criar(request.name(), request.description(), educator,
                request.price()));
    }
}
