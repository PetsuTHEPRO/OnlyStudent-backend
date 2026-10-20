package com.sloth.OnlyStudent.application.classroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
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
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;

@ExtendWith(MockitoExtension.class)
class GetClassroomDetailsUseCaseTest {

    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private ClassroomResponseMapper mapper;
    @InjectMocks
    private GetClassroomDetailsUseCase useCase;

    @Test
    void grantsAccessToOwnerAndEnrolledStudentAndRejectsOtherUsers() {
        Student student = new Student(7L, "Ana", "123", "ana@example.com", "hash", UserRole.STUDENT);
        Classroom classroom = new Classroom(12L, "Álgebra", "Aula", Status.ATIVA, Custo.GRATUITA, 0,
                new HashSet<>(List.of(student)),
                new Educator(3L, "Edu", "123", "edu@example.com", "hash", UserRole.EDUCATOR, "Matemática"),
                new HashSet<>());
        User owner = user(3L);
        User enrolled = user(7L);
        when(classroomRepository.findById(12L)).thenReturn(Optional.of(classroom));

        useCase.execute(12L, owner);
        useCase.execute(12L, enrolled);

        assertThrows(ClassroomAccessDeniedException.class, () -> useCase.execute(12L, user(99L)));
    }

    @Test
    void returnsNotFoundForMissingClassroom() {
        when(classroomRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ClassroomNotFoundException.class, () -> useCase.execute(99L, user(3L)));
    }

    private User user(Long id) {
        return new User(id, "User", "123", "user@example.com", "hash", UserRole.STUDENT);
    }
}
