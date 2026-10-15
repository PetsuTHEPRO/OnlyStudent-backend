package com.sloth.OnlyStudent.application.classroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
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
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.AlunoJaMatriculadoException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;
import com.sloth.OnlyStudent.shared.errors.StudentNotFoundException;

@ExtendWith(MockitoExtension.class)
class JoinClassroomUseCaseTest {

    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private JoinClassroomUseCase useCase;

    @Test
    void enrollsTheStudentIdentifiedByThePrincipal() {
        Classroom classroom = classroom();
        Student principalStudent = student(7L);
        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom));
        when(studentRepository.findById(7L)).thenReturn(Optional.of(principalStudent));

        useCase.execute(12L, 7L, UserRole.STUDENT);

        assertEquals(1, classroom.getAlunos().size());
        verify(classroomRepository).save(classroom);
    }

    @Test
    void rejectsDuplicateEnrollmentAndMissingEntities() {
        Classroom classroom = classroom();
        Student student = student(7L);
        classroom.getAlunos().add(student);
        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom));
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        assertThrows(AlunoJaMatriculadoException.class,
                () -> useCase.execute(12L, 7L, UserRole.STUDENT));

        when(classroomRepository.findByCodigo(99L)).thenReturn(Optional.empty());
        assertThrows(ClassroomNotFoundException.class,
                () -> useCase.execute(99L, 7L, UserRole.STUDENT));

        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom()));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(StudentNotFoundException.class,
                () -> useCase.execute(12L, 99L, UserRole.STUDENT));
    }

    private Classroom classroom() {
        return new Classroom(12L, "Álgebra", "Aula", Status.ATIVA, Custo.GRATUITA, 0,
                new HashSet<>(), new Educator(3L, "Edu", "123", "edu@example.com", "hash",
                        UserRole.EDUCATOR, "Matemática"), new HashSet<>());
    }

    private Student student(Long id) {
        return new Student(id, "Ana", "123", "ana@example.com", "hash", UserRole.STUDENT);
    }
}
