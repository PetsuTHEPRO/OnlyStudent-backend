package com.sloth.OnlyStudent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.application.material.CreateMaterialUseCase;
import com.sloth.OnlyStudent.application.material.DeleteMaterialUseCase;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("material")
@Validated
public class MaterialController {

    private final CreateMaterialUseCase createMaterialUseCase;
    private final DeleteMaterialUseCase deleteMaterialUseCase;

    public MaterialController(CreateMaterialUseCase createMaterialUseCase,
            DeleteMaterialUseCase deleteMaterialUseCase) {
        this.createMaterialUseCase = createMaterialUseCase;
        this.deleteMaterialUseCase = deleteMaterialUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerMaterial(@Valid @RequestBody MaterialDTO body,
            @AuthenticationPrincipal User authenticatedUser) {
        createMaterialUseCase.execute(body, authenticatedUser.getId(), authenticatedUser.getRoles());
        return ResponseEntity.ok("Material registered successfully!");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMaterial(@PathVariable long id,
            @AuthenticationPrincipal User authenticatedUser) {
        deleteMaterialUseCase.execute(id, authenticatedUser.getId(), authenticatedUser.getRoles());
        return ResponseEntity.ok("Material deleted successfully.");
    }
}
