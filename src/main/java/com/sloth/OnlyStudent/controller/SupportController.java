package com.sloth.OnlyStudent.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.DTO.SupportDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class SupportController {

	@Autowired
	private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(remetente);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates the text is HTML
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            // You might want to handle the exception properly here
        }
    }

    @PostMapping("/send")
    public String sendSupportEmail(@RequestBody SupportDTO supportDTO) {
        String subject = "Requisição de Suporte - email: " + supportDTO.email();
        String htmlContent = generateHtmlContent(supportDTO);

        this.sendEmail(remetente, subject, htmlContent);

        return "Email sent successfully";
    }

    private String generateHtmlContent(SupportDTO supportDTO) {
        return "<!DOCTYPE html>" +
                "<html lang=\"pt-BR\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\">" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "  <title>Solicitação de Suporte</title>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; color: #333; margin: 0; padding: 20px; background-color: #f4f4f4; }" +
                "    .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border: 1px solid #ddd; border-radius: 5px; }" +
                "    .header { text-align: center; margin-bottom: 20px; }" +
                "    .header h1 { margin: 0; color: #007bff; }" +
                "    .section { margin-bottom: 20px; }" +
                "    .section h2 { margin: 0 0 10px; color: #007bff; }" +
                "    .section p { margin: 5px 0; }" +
                "    .footer { margin-top: 20px; text-align: center; font-size: 14px; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class=\"container\">" +
                "    <div class=\"header\">" +
                "      <h1>Nova Solicitação de Suporte</h1>" +
                "    </div>" +
                "    <div class=\"section\">" +
                "      <h2>Detalhes do Suporte</h2>" +
                "      <p><strong>🧑‍💼Nome do Solicitador:</strong> " + supportDTO.name() + "</p>" +
                "      <p><strong>📧 Email para Contato:</strong> " + supportDTO.email() + "</p>" +
                "      <p><strong>📞 Telefone para Contato:</strong> " + supportDTO.phone() + "</p>" +
                "      <p><strong>📝 Motivação:</strong> " + supportDTO.razao() + "</p>" +
                "      <p><strong>📝 Descrição:</strong></p>" +
                "      <p>" + supportDTO.descricao() + "</p>" +
                "    </div>" +
                "    <div class=\"footer\">" +
                "      <p>Atenciosamente, Only Study</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
}