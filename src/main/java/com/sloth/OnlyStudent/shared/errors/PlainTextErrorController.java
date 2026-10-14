package com.sloth.OnlyStudent.shared.errors;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PlainTextErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Object statusAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = statusAttribute instanceof Integer code ? code : HttpStatus.INTERNAL_SERVER_ERROR.value();
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null || status.is5xxServerError()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno.");
        }
        if (status == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(status).body("Recurso não encontrado.");
        }
        if (status.is4xxClientError()) {
            return ResponseEntity.status(status).body("Requisição inválida.");
        }
        return ResponseEntity.status(status).body("Erro interno.");
    }
}
