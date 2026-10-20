package com.sloth.OnlyStudent.application.material;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.MaterialAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.MaterialNotFoundException;

@Service
public class DeleteMaterialUseCase {

    private final MaterialRepository materialRepository;

    public DeleteMaterialUseCase(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional
    public void execute(Long materialId, Long principalId, UserRole principalRole) {
        if (principalRole != UserRole.EDUCATOR) {
            throw new MaterialAccessDeniedException();
        }
        Material material = materialRepository.findById(materialId)
                .orElseThrow(MaterialNotFoundException::new);
        if (material.getTurma() == null || material.getTurma().getEducator() == null
                || !Objects.equals(material.getTurma().getEducator().getId(), principalId)) {
            throw new MaterialAccessDeniedException();
        }
        materialRepository.delete(material);
    }
}
