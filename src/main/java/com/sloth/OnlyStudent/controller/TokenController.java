package com.sloth.OnlyStudent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.infra.security.TokenService;

@RestController
@RequestMapping("token")
public class TokenController {

	@Autowired
	private TokenService tokenService;
	
	@GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        try {
            String email = tokenService.validateToken(token);
            if (email != null) {
                return ResponseEntity.ok("Token is valid.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired.");
        }
    }
}
