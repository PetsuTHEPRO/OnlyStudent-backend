package com.sloth.OnlyStudent.application.classroom;

import com.sloth.OnlyStudent.entities.Aviso;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAvisoUseCase {

    private final ClassroomRepository classroomRepository;

    public CreateAvisoUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public void execute(Long turmaId, String mensagem, User principal) {
        if (principal.getRole() != UserRole.EDUCATOR) {
            throw new ClassroomAccessDeniedException();
        }

        Classroom classroom = classroomRepository.findById(turmaId)
                .orElseThrow(ClassroomNotFoundException::new);

        if (!classroom.getEducator().getId().equals(principal.getId())) {
            throw new ClassroomAccessDeniedException();
        }

        Aviso aviso = new Aviso(mensagem, classroom);
        classroom.getAvisos().add(aviso);
        classroomRepository.save(classroom);
    }
}