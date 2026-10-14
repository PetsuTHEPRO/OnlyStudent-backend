package com.sloth.OnlyStudent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.services.AuthenticationService;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO body) {
        return authenticationService.authenticateUser(body);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterDTO body) {
        return authenticationService.createUser(body);
    }

    @GetMapping("/educator")
    @PreAuthorize("hasRole('EDUCATOR')")
    public String getEducatorResource() {
        return "Resource for Educators";
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public String getStudentResource() {
        return "Resource for Students";
    }

    @GetMapping("/common")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDUCATOR', 'STUDENT')")
    public String getCommonResource() {
        return "Resource for All Users";
    }

}
