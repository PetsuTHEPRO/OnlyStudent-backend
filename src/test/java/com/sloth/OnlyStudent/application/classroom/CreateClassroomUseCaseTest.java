package com.sloth.OnlyStudent.application.classroom;

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
import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNameAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class CreateClassroomUseCaseTest {

    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private EducatorRepository educatorRepository;
    @InjectMocks
    private CreateClassroomUseCase useCase;

    @Test
    void createsAnActiveClassroomUsingThePrincipalEducatorAndDerivedCost() {
        Educator principalEducator = educator(8L);
        when(classroomRepository.existsByName("Álgebra")).thenReturn(false);
        when(educatorRepository.findById(8L)).thenReturn(Optional.of(principalEducator));
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Classroom created = useCase.execute(new ClassroomDTO("Álgebra", "Aula", 777L, 0),
                8L, UserRole.EDUCATOR);

        assertEquals(8L, created.getEducator().getId());
        assertEquals(Status.ATIVA, created.getStatus());
        assertEquals(Custo.GRATUITA, created.getCusto());
        verify(classroomRepository).save(created);
    }

    @Test
    void rejectsDuplicateNamesAndNonEducatorPrincipals() {
        when(classroomRepository.existsByName("Álgebra")).thenReturn(true);
        assertThrows(ClassroomNameAlreadyExistsException.class, () -> useCase.execute(
                new ClassroomDTO("Álgebra", "Aula", 1L, 0), 8L, UserRole.EDUCATOR));
        assertThrows(ClassroomAccessDeniedException.class, () -> useCase.execute(
                new ClassroomDTO("Outra", "Aula", 1L, 0), 8L, UserRole.STUDENT));
    }

    private Educator educator(Long id) {
        return new Educator(id, "Educador", "123", "edu@example.com", "hash", UserRole.EDUCATOR, "Matemática");
    }
}
