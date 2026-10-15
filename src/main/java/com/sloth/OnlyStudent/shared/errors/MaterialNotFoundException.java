package com.sloth.OnlyStudent.shared.errors;

public class MaterialNotFoundException extends RuntimeException {

    public MaterialNotFoundException() {
        super("Material not found.");
    }
}
