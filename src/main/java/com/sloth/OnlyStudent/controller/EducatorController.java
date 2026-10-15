package com.sloth.OnlyStudent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.application.profile.GetEducatorProfileUseCase;
import com.sloth.OnlyStudent.application.profile.UpdateEducatorProfileUseCase;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.entities.DTO.EducatorResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("educator")
@RequiredArgsConstructor
public class EducatorController {

    private final GetEducatorProfileUseCase getEducatorProfileUseCase;
    private final UpdateEducatorProfileUseCase updateEducatorProfileUseCase;

    @GetMapping("/idByEmail")
    public ResponseEntity<Long> getEducatorIdByEmail(@RequestParam(required = false) String email,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(authenticatedUser.getId());
    }

    @GetMapping("/atualUser")
    public ResponseEntity<EducatorResponseDTO> getUser(@RequestParam(required = false) Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        return getEducatorProfileUseCase.execute(authenticatedUser.getId(), id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<String> updateUserByEmail(@RequestParam(required = false) String login,
            @RequestBody EducatorDTO user, @AuthenticationPrincipal User authenticatedUser) {
        return updateEducatorProfileUseCase.execute(authenticatedUser.getId(), login, user)
                .map(updatedProfile -> ResponseEntity.ok("Salvado com Sucesso!"))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
