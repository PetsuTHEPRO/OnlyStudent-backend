package com.sloth.OnlyStudent.shared.errors;

public class MaterialAccessDeniedException extends RuntimeException {

    public MaterialAccessDeniedException() {
        super("Apenas o educador responsável pode alterar esta turma.");
    }
}
