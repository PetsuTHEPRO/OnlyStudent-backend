package com.sloth.OnlyStudent.shared.errors;

public class EducatorNotFoundException extends RuntimeException {

    public EducatorNotFoundException() {
        super("Educador não encontrado.");
    }
}
