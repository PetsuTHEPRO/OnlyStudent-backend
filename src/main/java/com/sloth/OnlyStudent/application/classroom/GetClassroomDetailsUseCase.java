package com.sloth.OnlyStudent.application.classroom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDetailsDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;

@Service
public class GetClassroomDetailsUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomResponseMapper mapper;

    public GetClassroomDetailsUseCase(ClassroomRepository classroomRepository, ClassroomResponseMapper mapper) {
        this.classroomRepository = classroomRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ClassroomDetailsDTO execute(Long classroomId, User principal) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(ClassroomNotFoundException::new);
        if (!classroom.podeSerAcessadaPor(principal)) {
            throw new ClassroomAccessDeniedException();
        }
        return mapper.toDetails(classroom);
    }
}
