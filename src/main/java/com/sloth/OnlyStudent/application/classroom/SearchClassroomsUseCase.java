package com.sloth.OnlyStudent.application.classroom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;

@Service
public class SearchClassroomsUseCase {

    private final ClassroomRepository classroomRepository;
    private final ClassroomResponseMapper mapper;

    public SearchClassroomsUseCase(ClassroomRepository classroomRepository, ClassroomResponseMapper mapper) {
        this.classroomRepository = classroomRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<ClassroomsDTO> execute(String search, Pageable pageable) {
        return classroomRepository.findByNameContainingIgnoreCase(search, pageable).map(mapper::toSummary);
    }
}
