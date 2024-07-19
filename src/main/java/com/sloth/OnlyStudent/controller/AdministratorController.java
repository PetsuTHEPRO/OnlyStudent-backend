package com.sloth.OnlyStudent.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;

@RestController
@RequestMapping("admin")
public class AdministratorController {
	
    @Autowired
    private AdministratorRepository repository;
    @Autowired
	private PasswordEncoder passwordEncoder;
    
    // Listar todos os administradores
    @GetMapping
    public List<Administrator> getAll() {
        return repository.findAll();
    }
    
    // Obter um administrador específico por ID
    @GetMapping("/{id}")
    public ResponseEntity<Administrator> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(admin -> ResponseEntity.ok().body(admin))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Criar um novo administrador
    @PostMapping
    public Administrator create(@RequestBody RegisterDTO administrator) {
        return repository.save(new Administrator(null, administrator.name(), administrator.login(), passwordEncoder.encode(administrator.password()), administrator.role()));
    }
    
    // Atualizar um administrador existente
    @PutMapping("/{id}")
    public ResponseEntity<Administrator> update(@PathVariable Long id, @RequestBody RegisterDTO administratorDetails) {
        return repository.findById(id)
                .map(admin -> {
                    admin.setName(administratorDetails.name());
                    admin.setLogin(administratorDetails.login());
                    // Adicione outras atualizações de campo conforme necessário
                    Administrator updatedAdmin = repository.save(admin);
                    return ResponseEntity.ok().body(updatedAdmin);
                }).orElse(ResponseEntity.notFound().build());
    }
    
    // Deletar um administrador
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repository.findById(id)
                .map(admin -> {
                    repository.delete(admin);
                    return ResponseEntity.ok().build();
                }).orElse(ResponseEntity.notFound().build());
    }
    
    // Recurso específico para administradores
    @GetMapping("/admin-resource")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminResource() {
        return "Resource for Admins";
    }
}
