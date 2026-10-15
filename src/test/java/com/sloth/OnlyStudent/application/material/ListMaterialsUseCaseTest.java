package com.sloth.OnlyStudent.application.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ListMaterialsUseCaseTest {

    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MaterialResponseMapper mapper;
    @InjectMocks
    private ListMaterialsUseCase useCase;

    @Test
    void ownerOrEnrolledStudentGetsAllMaterialsMappedToContractDto() {
        Classroom classroom = classroom(7L, true);
        Material material = org.mockito.Mockito.mock(Material.class);
        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom));
        when(materialRepository.findByTurmaCodigo(12L, Pageable.unpaged())).thenReturn(List.of(material));
        when(mapper.toResponse(material)).thenReturn(
                new com.sloth.OnlyStudent.entities.DTO.MaterialResponseDTO(3L,
                        com.sloth.OnlyStudent.entities.MaterialType.PDF, "Apostila", "https://example.com/a.pdf"));

        var response = useCase.execute(12L, 7L, Pageable.unpaged());
        assertEquals(1, response.size());
        assertEquals(3L, response.get(0).id());
        assertEquals("Apostila", response.get(0).name());
        verify(materialRepository).findByTurmaCodigo(12L, Pageable.unpaged());

        var enrolledResponse = useCase.execute(12L, 8L, Pageable.unpaged());
        assertEquals(1, enrolledResponse.size());
    }

    @Test
    void rejectsStudentNotEnrolledInClassroom() {
        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom(7L, false)));
        assertThrows(ClassroomAccessDeniedException.class,
                () -> useCase.execute(12L, 9L, Pageable.unpaged()));
    }

    private Classroom classroom(Long educatorId, boolean includeStudent) {
        Educator educator = new Educator(educatorId, "Educator", "111", "educator@example.com",
                "hash", UserRole.EDUCATOR, "Matemática");
        Classroom classroom = Classroom.criar("Álgebra", "Aula", educator, 0);
        if (includeStudent) {
            classroom.getAlunos().add(new Student(8L, "Student", "222", "student@example.com",
                    "hash", UserRole.STUDENT));
        }
        return classroom;
    }
}
