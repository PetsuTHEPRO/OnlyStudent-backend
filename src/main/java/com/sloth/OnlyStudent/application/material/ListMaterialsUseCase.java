package com.sloth.OnlyStudent.application.material;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.DTO.MaterialResponseDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;

@Service
public class ListMaterialsUseCase {

    private final ClassroomRepository classroomRepository;
    private final MaterialRepository materialRepository;
    private final MaterialResponseMapper mapper;

    public ListMaterialsUseCase(ClassroomRepository classroomRepository, MaterialRepository materialRepository,
            MaterialResponseMapper mapper) {
        this.classroomRepository = classroomRepository;
        this.materialRepository = materialRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> execute(Long classroomCode, Long principalId, Pageable pageable) {
        Classroom classroom = classroomRepository.findByCodigo(classroomCode)
                .orElseThrow(ClassroomNotFoundException::new);
        if (!classroom.podeSerAcessadaPor(principalId)) {
            throw new ClassroomAccessDeniedException();
        }

        return materialRepository.findByTurmaCodigo(classroomCode, pageable).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
