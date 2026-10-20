package com.sloth.OnlyStudent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.application.profile.GetStudentProfileUseCase;
import com.sloth.OnlyStudent.application.profile.UpdateStudentProfileUseCase;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentResponseDTO;

@RestController
@RequestMapping("student")
public class StudentController {

    private final GetStudentProfileUseCase getStudentProfileUseCase;
    private final UpdateStudentProfileUseCase updateStudentProfileUseCase;

    public StudentController(GetStudentProfileUseCase getStudentProfileUseCase,
            UpdateStudentProfileUseCase updateStudentProfileUseCase) {
        this.getStudentProfileUseCase = getStudentProfileUseCase;
        this.updateStudentProfileUseCase = updateStudentProfileUseCase;
    }

    @GetMapping("/atualUser")
    public ResponseEntity<StudentResponseDTO> getUser(@RequestParam(required = false) Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        return getStudentProfileUseCase.execute(authenticatedUser.getId(), id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<String> updateUserByEmail(@RequestParam(required = false) String login,
            @RequestBody EducatorDTO user, @AuthenticationPrincipal User authenticatedUser) {
        return updateStudentProfileUseCase.execute(authenticatedUser.getId(), login, user)
                .map(updatedProfile -> ResponseEntity.ok("Salvado com Sucesso!"))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
