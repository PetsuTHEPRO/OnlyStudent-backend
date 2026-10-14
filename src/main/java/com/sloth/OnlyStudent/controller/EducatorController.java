package com.sloth.OnlyStudent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.repository.EducatorRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("educator")
@RequiredArgsConstructor
public class EducatorController {
	
    @Autowired
    private EducatorRepository educatorRepository;
    
    @GetMapping("/idByEmail")
    public ResponseEntity<Long> getEducatorIdByEmail(@RequestParam(required = false) String email,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(authenticatedUser.getId());
    }
	
    @GetMapping("/atualUser")
    public ResponseEntity<EducatorDTO> getUser(@RequestParam(required = false) Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        return educatorRepository.findById(authenticatedUser.getId())
                .map(educator -> ResponseEntity.ok(new EducatorDTO(educator.getName(), educator.getTelephone(),
                        educator.getEmail(), educator.getEspecialidade())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
	
    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateUserByEmail(@RequestParam(required = false) String login,
            @RequestBody EducatorDTO user, @AuthenticationPrincipal User authenticatedUser) {
        return educatorRepository.findById(authenticatedUser.getId())
                .map(educator -> {
                    educator.setName(user.name());
                    educator.setTelephone(user.telefone());
                    educator.setEspecialidade(user.especialidade());
                    educatorRepository.save(educator);
                    return ResponseEntity.ok("Salvado com Sucesso!");
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
