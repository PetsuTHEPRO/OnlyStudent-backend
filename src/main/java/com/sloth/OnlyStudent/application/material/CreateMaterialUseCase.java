package com.sloth.OnlyStudent.application.material;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;
import com.sloth.OnlyStudent.shared.errors.MaterialAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.MaterialAlreadyExistsException;

@Service
public class CreateMaterialUseCase {

    private final ClassroomRepository classroomRepository;
    private final MaterialRepository materialRepository;

    public CreateMaterialUseCase(ClassroomRepository classroomRepository, MaterialRepository materialRepository) {
        this.classroomRepository = classroomRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    public Material execute(MaterialDTO request, Long principalId, UserRole principalRole) {
        if (principalRole != UserRole.EDUCATOR) {
            throw new MaterialAccessDeniedException();
        }
        Classroom classroom = classroomRepository.findByCodigo(request.turmaCodigo())
                .orElseThrow(ClassroomNotFoundException::new);
        if (classroom.getEducator() == null
                || !Objects.equals(classroom.getEducator().getId(), principalId)) {
            throw new MaterialAccessDeniedException();
        }
        if (materialRepository.existsByNameAndTurmaCodigo(request.name(), request.turmaCodigo())) {
            throw new MaterialAlreadyExistsException();
        }

        return materialRepository.save(Material.criar(request.tipo(), request.name(), request.url(), classroom));
    }
}
