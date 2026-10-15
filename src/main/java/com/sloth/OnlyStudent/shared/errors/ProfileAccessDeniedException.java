package com.sloth.OnlyStudent.shared.errors;

public class ProfileAccessDeniedException extends RuntimeException {

    public ProfileAccessDeniedException() {
        super("Acesso negado.");
    }
}
