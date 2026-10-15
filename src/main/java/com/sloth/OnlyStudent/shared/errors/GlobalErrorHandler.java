package com.sloth.OnlyStudent.shared.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Requisição inválida.");
        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body("Requisição inválida.");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Email não cadastrado, acesse a página de cadastro!");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.badRequest().body("Senha incorreta.");
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado.");
    }

    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<String> handleInvalidRegistration(InvalidRegistrationException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(ClassroomNotFoundException.class)
    public ResponseEntity<String> handleClassroomNotFound(ClassroomNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<String> handleStudentNotFound(StudentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(EducatorNotFoundException.class)
    public ResponseEntity<String> handleEducatorNotFound(EducatorNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ProfileAccessDeniedException.class)
    public ResponseEntity<String> handleProfileAccessDenied(ProfileAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(ClassroomAccessDeniedException.class)
    public ResponseEntity<String> handleClassroomAccessDenied(ClassroomAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(ClassroomNameAlreadyExistsException.class)
    public ResponseEntity<String> handleClassroomNameAlreadyExists(ClassroomNameAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(AlunoJaMatriculadoException.class)
    public ResponseEntity<String> handleAlreadyEnrolled(AlunoJaMatriculadoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(InvalidClassroomException.class)
    public ResponseEntity<String> handleInvalidClassroom(InvalidClassroomException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(InvalidMaterialException.class)
    public ResponseEntity<String> handleInvalidMaterial(InvalidMaterialException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(MaterialNotFoundException.class)
    public ResponseEntity<String> handleMaterialNotFound(MaterialNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(MaterialAccessDeniedException.class)
    public ResponseEntity<String> handleMaterialAccessDenied(MaterialAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(MaterialAlreadyExistsException.class)
    public ResponseEntity<String> handleMaterialAlreadyExists(MaterialAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause().getMessage();
        if (message != null && message.contains("uk_classroom_student")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Você já está matriculado nesta turma.");
        }
        logger.warn("Database constraint violation: {}", message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito de dados.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception exception) {
        logger.error("Unexpected error while processing request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno.");
    }
}
