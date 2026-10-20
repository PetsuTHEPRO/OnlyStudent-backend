package com.sloth.OnlyStudent.application.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.MaterialAlreadyExistsException;
import com.sloth.OnlyStudent.shared.errors.MaterialAccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CreateMaterialUseCaseTest {

    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private MaterialRepository materialRepository;
    @InjectMocks
    private CreateMaterialUseCase useCase;

    @Test
    void ownerEducatorCreatesMaterialFromRequest() {
        Classroom classroom = classroom(7L);
        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom));
        when(materialRepository.existsByNameAndTurmaCodigo("Apostila", 12L)).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material material = useCase.execute(
                new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 12L),
                7L, UserRole.EDUCATOR);

        assertEquals("Apostila", material.getName());
        assertEquals(MaterialType.PDF, material.getTipo());
        assertEquals(classroom, material.getTurma());
        verify(materialRepository).save(material);
    }

    @Test
    void rejectsWrongRoleNonOwnerAndDuplicateName() {
        assertThrows(MaterialAccessDeniedException.class, () -> useCase.execute(
                new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 12L),
                7L, UserRole.STUDENT));

        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom(7L)));
        assertThrows(MaterialAccessDeniedException.class, () -> useCase.execute(
                new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 12L),
                8L, UserRole.EDUCATOR));

        when(materialRepository.existsByNameAndTurmaCodigo("Apostila", 12L)).thenReturn(true);
        assertThrows(MaterialAlreadyExistsException.class, () -> useCase.execute(
                new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 12L),
                7L, UserRole.EDUCATOR));
    }

    private Classroom classroom(Long educatorId) {
        Educator educator = new Educator(educatorId, "Educator", "111", "educator@example.com",
                "hash", UserRole.EDUCATOR, "Matemática");
        return Classroom.criar("Álgebra", "Aula", educator, 0);
    }
}
