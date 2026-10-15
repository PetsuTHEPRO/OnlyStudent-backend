package com.sloth.OnlyStudent.shared.errors;

public class ClassroomAccessDeniedException extends RuntimeException {

    public ClassroomAccessDeniedException() {
        super("Acesso negado.");
    }
}
