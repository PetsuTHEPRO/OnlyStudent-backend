package com.sloth.OnlyStudent.application.classroom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;
import com.sloth.OnlyStudent.shared.errors.StudentNotFoundException;

@Service
public class JoinClassroomUseCase {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;

    public JoinClassroomUseCase(ClassroomRepository classroomRepository, StudentRepository studentRepository) {
        this.classroomRepository = classroomRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public void execute(Long classroomCode, Long principalId, UserRole principalRole) {
        if (principalRole != UserRole.STUDENT) {
            throw new ClassroomAccessDeniedException();
        }
        Classroom classroom = classroomRepository.findByCodigo(classroomCode)
                .orElseThrow(ClassroomNotFoundException::new);
        Student student = studentRepository.findById(principalId)
                .orElseThrow(StudentNotFoundException::new);
        classroom.matricular(student);
        classroomRepository.save(classroom);
    }
}
