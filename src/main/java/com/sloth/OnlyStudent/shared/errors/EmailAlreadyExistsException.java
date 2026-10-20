package com.sloth.OnlyStudent.shared.errors;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
    }

    public EmailAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
