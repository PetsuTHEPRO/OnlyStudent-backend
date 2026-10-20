package com.sloth.OnlyStudent.shared.errors;

public class SupportDeliveryException extends RuntimeException {

    public SupportDeliveryException(Throwable cause) {
        super("Falha ao enviar email de suporte.", cause);
    }

    public SupportDeliveryException(String message) {
        super(message);
    }
}