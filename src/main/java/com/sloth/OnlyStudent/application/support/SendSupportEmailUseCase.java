package com.sloth.OnlyStudent.application.support;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.sloth.OnlyStudent.entities.DTO.SupportDTO;
import com.sloth.OnlyStudent.shared.errors.SupportDeliveryException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SendSupportEmailUseCase {

    private final JavaMailSender emailSender;
    private final SupportEmailContentMapper contentMapper;
    private final String senderAddress;

    public SendSupportEmailUseCase(JavaMailSender emailSender, SupportEmailContentMapper contentMapper,
            @Value("${spring.mail.username}") String senderAddress) {
        this.emailSender = emailSender;
        this.contentMapper = contentMapper;
        this.senderAddress = senderAddress;
    }

    public void execute(SupportDTO request) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(Objects.requireNonNull(senderAddress, "senderAddress"));
            helper.setTo(Objects.requireNonNull(senderAddress, "senderAddress"));
            helper.setSubject(Objects.requireNonNull(contentMapper.subject(request), "subject"));
            helper.setText(Objects.requireNonNull(contentMapper.toHtml(request), "htmlContent"), true);
            emailSender.send(message);
        } catch (MessagingException exception) {
            throw new SupportDeliveryException(exception);
        }
    }
}
