package com.sloth.OnlyStudent.shared.errors;

public class MaterialAlreadyExistsException extends RuntimeException {

    public MaterialAlreadyExistsException() {
        super("Material already exists for this classroom.");
    }
}
