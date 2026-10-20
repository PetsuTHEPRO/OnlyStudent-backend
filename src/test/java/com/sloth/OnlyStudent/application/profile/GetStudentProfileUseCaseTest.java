package com.sloth.OnlyStudent.application.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

class GetStudentProfileUseCaseTest {

    private final StudentRepository studentRepository = mock(StudentRepository.class);
    private final GetStudentProfileUseCase useCase =
            new GetStudentProfileUseCase(studentRepository, new StudentProfileMapper());

    @Test
    void returnsProfileForAuthenticatedStudentId() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student(7L)));

        assertEquals("Ana", useCase.execute(7L, 7L).orElseThrow().name());
        verify(studentRepository).findById(7L);
    }

    @Test
    void rejectsARequestedIdDifferentFromAuthenticatedStudent() {
        assertThrows(ProfileAccessDeniedException.class, () -> useCase.execute(7L, 8L));
        verifyNoInteractions(studentRepository);
    }

    private Student student(Long id) {
        return new Student(id, "Ana", "111", "ana@example.com", "hash", UserRole.STUDENT);
    }
}
