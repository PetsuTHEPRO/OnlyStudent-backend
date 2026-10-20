package com.sloth.OnlyStudent.application.support;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.sloth.OnlyStudent.entities.DTO.SupportDTO;

@Component
public class SupportEmailContentMapper {

    public String subject(SupportDTO request) {
        return "Requisição de Suporte - email: " + Objects.requireNonNull(request.email(), "email");
    }

    public String toHtml(SupportDTO request) {
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
                "      <p><strong>🧑‍💼Nome do Solicitador:</strong> " + HtmlUtils.htmlEscape(Objects.requireNonNull(request.name(), "name")) + "</p>" +
                "      <p><strong>📧 Email para Contato:</strong> " + HtmlUtils.htmlEscape(Objects.requireNonNull(request.email(), "email")) + "</p>" +
                "      <p><strong>📞 Telefone para Contato:</strong> " + HtmlUtils.htmlEscape(Objects.requireNonNull(request.phone(), "phone")) + "</p>" +
                "      <p><strong>📝 Motivação:</strong> " + HtmlUtils.htmlEscape(Objects.requireNonNull(request.razao(), "razao")) + "</p>" +
                "      <p><strong>📝 Descrição:</strong></p>" +
                "      <p>" + HtmlUtils.htmlEscape(Objects.requireNonNull(request.descricao(), "descricao")) + "</p>" +
                "    </div>" +
                "    <div class=\"footer\">" +
                "      <p>Atenciosamente, Only Study</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
}
