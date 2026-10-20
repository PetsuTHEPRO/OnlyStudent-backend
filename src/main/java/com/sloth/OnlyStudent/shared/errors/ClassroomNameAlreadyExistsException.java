package com.sloth.OnlyStudent.shared.errors;

public class ClassroomNameAlreadyExistsException extends RuntimeException {

    public ClassroomNameAlreadyExistsException() {
        super("Já existe uma turma com esse nome.");
    }
}
