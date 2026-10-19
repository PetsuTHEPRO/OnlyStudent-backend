package com.sloth.OnlyStudent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.application.administrator.CreateAdministratorUseCase;
import com.sloth.OnlyStudent.application.administrator.DeleteAdministratorUseCase;
import com.sloth.OnlyStudent.application.administrator.GetAdministratorUseCase;
import com.sloth.OnlyStudent.application.administrator.ListAdministratorsUseCase;
import com.sloth.OnlyStudent.application.administrator.UpdateAdministratorUseCase;
import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("admin")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdministratorController {

    private final ListAdministratorsUseCase listAdministratorsUseCase;
    private final GetAdministratorUseCase getAdministratorUseCase;
    private final CreateAdministratorUseCase createAdministratorUseCase;
    private final UpdateAdministratorUseCase updateAdministratorUseCase;
    private final DeleteAdministratorUseCase deleteAdministratorUseCase;

    public AdministratorController(ListAdministratorsUseCase listAdministratorsUseCase,
            GetAdministratorUseCase getAdministratorUseCase,
            CreateAdministratorUseCase createAdministratorUseCase,
            UpdateAdministratorUseCase updateAdministratorUseCase,
            DeleteAdministratorUseCase deleteAdministratorUseCase) {
        this.listAdministratorsUseCase = listAdministratorsUseCase;
        this.getAdministratorUseCase = getAdministratorUseCase;
        this.createAdministratorUseCase = createAdministratorUseCase;
        this.updateAdministratorUseCase = updateAdministratorUseCase;
        this.deleteAdministratorUseCase = deleteAdministratorUseCase;
    }

    @GetMapping
    public List<AdministratorResponseDTO> getAll() {
        return listAdministratorsUseCase.execute();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministratorResponseDTO> getById(@PathVariable Long id) {
        return getAdministratorUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public AdministratorResponseDTO create(@Valid @RequestBody RegisterDTO administrator) {
        return createAdministratorUseCase.execute(administrator);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministratorResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody RegisterDTO administratorDetails) {
        return updateAdministratorUseCase.execute(id, administratorDetails)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return deleteAdministratorUseCase.execute(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/admin-resource")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminResource() {
        return "Resource for Admins";
    }
}
