package com.sloth.OnlyStudent.shared.errors;

public class AlunoJaMatriculadoException extends RuntimeException {

    public AlunoJaMatriculadoException() {
        super("Você já está matriculado nesta turma.");
    }
}
