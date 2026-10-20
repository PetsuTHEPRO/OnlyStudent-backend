package com.sloth.OnlyStudent.application.support;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import com.sloth.OnlyStudent.entities.DTO.SupportDTO;
import com.sloth.OnlyStudent.shared.errors.SupportDeliveryException;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

class SendSupportEmailUseCaseTest {

    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final SupportEmailContentMapper mapper = new SupportEmailContentMapper();

    @SuppressWarnings("unchecked")
    private ObjectProvider<JavaMailSender> providerReturning(JavaMailSender value) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    @Test
    void sendsAnHtmlEmailToTheConfiguredSupportAddress() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(message);
        SendSupportEmailUseCase useCase =
                new SendSupportEmailUseCase(providerReturning(sender), mapper, "support@example.com");

        useCase.execute(validRequest());

        verify(sender).send(message);
    }

    @Test
    void reportsMessageConstructionErrorsInsteadOfReturningSuccess() throws Exception {
        MimeMessage message = mock(MimeMessage.class);
        when(sender.createMimeMessage()).thenReturn(message);
        when(message.getSession()).thenReturn(Session.getInstance(new Properties()));
        org.mockito.Mockito.doThrow(new MessagingException("bad mime message"))
                .when(message).setFrom(org.mockito.ArgumentMatchers.any(jakarta.mail.internet.InternetAddress.class));
        SendSupportEmailUseCase useCase =
                new SendSupportEmailUseCase(providerReturning(sender), mapper, "support@example.com");

        assertThrows(SupportDeliveryException.class, () -> useCase.execute(validRequest()));
    }

    @Test
    void failsWithClearMessageWhenMailSenderIsNotConfigured() {
        SendSupportEmailUseCase useCase =
                new SendSupportEmailUseCase(providerReturning(null), mapper, "support@example.com");

        SupportDeliveryException exception =
                assertThrows(SupportDeliveryException.class, () -> useCase.execute(validRequest()));

        org.junit.jupiter.api.Assertions.assertEquals(
                "Serviço de e-mail não configurado.", exception.getMessage());
    }

    private SupportDTO validRequest() {
        return new SupportDTO("Name", "user@example.com", "555", "Reason", "Description");
    }
}