package com.sloth.OnlyStudent.shared.errors;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException() {
        super("Aluno não encontrado.");
    }
}
