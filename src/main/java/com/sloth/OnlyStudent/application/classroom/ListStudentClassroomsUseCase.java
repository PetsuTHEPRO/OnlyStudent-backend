package com.sloth.OnlyStudent.application.classroom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;

@Service
public class ListStudentClassroomsUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomResponseMapper mapper;

    public ListStudentClassroomsUseCase(ClassroomRepository classroomRepository, ClassroomResponseMapper mapper) {
        this.classroomRepository = classroomRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<ClassroomsDTO> execute(Long principalId, Pageable pageable) {
        return classroomRepository.findByStudentId(principalId, pageable).map(mapper::toSummary);
    }
}
