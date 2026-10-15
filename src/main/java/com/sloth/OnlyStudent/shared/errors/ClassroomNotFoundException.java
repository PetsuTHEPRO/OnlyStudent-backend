package com.sloth.OnlyStudent.shared.errors;

public class ClassroomNotFoundException extends RuntimeException {

    public ClassroomNotFoundException() {
        super("Turma não encontrada.");
    }
}
