package com.sloth.OnlyStudent.application.material;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.MaterialType;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.MaterialAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.MaterialNotFoundException;

@ExtendWith(MockitoExtension.class)
class DeleteMaterialUseCaseTest {

    @Mock
    private MaterialRepository materialRepository;
    @InjectMocks
    private DeleteMaterialUseCase useCase;

    @Test
    void ownerEducatorDeletesMaterial() {
        Material material = materialForEducator(7L);
        when(materialRepository.findById(19L)).thenReturn(Optional.of(material));

        useCase.execute(19L, 7L, UserRole.EDUCATOR);

        verify(materialRepository).delete(material);
    }

    @Test
    void rejectsNonOwnerWrongRoleAndMissingMaterial() {
        when(materialRepository.findById(19L)).thenReturn(Optional.of(materialForEducator(7L)));
        assertThrows(MaterialAccessDeniedException.class,
                () -> useCase.execute(19L, 8L, UserRole.EDUCATOR));
        assertThrows(MaterialAccessDeniedException.class,
                () -> useCase.execute(19L, 7L, UserRole.STUDENT));

        when(materialRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MaterialNotFoundException.class,
                () -> useCase.execute(99L, 7L, UserRole.EDUCATOR));
    }

    private Material materialForEducator(Long educatorId) {
        Educator educator = new Educator(educatorId, "Educator", "111", "educator@example.com",
                "hash", UserRole.EDUCATOR, "Matemática");
        Classroom classroom = Classroom.criar("Álgebra", "Aula", educator, 0);
        return Material.criar("PDF", "Apostila", "https://example.com/a.pdf", classroom);
    }
}
