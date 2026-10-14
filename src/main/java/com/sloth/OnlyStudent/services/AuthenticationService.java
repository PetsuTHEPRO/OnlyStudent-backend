package com.sloth.OnlyStudent.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.sloth.OnlyStudent.application.auth.LoginUserUseCase;
import com.sloth.OnlyStudent.application.auth.RegisterUserUseCase;
import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;

@Service
public class AuthenticationService {

    private final LoginUserUseCase loginUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthenticationService(LoginUserUseCase loginUserUseCase, RegisterUserUseCase registerUserUseCase) {
        this.loginUserUseCase = loginUserUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }

    public ResponseEntity<?> authenticateUser(AuthenticationDTO body) {
        return ResponseEntity.ok(loginUserUseCase.execute(body));
    }

    public ResponseEntity<Void> createUser(RegisterDTO body) {
        registerUserUseCase.execute(body);
        return ResponseEntity.status(201).build();
    }
}
