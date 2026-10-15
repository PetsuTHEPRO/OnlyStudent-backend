package com.sloth.OnlyStudent.application.administrator;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;

@Service
public class ListAdministratorsUseCase {

    private final AdministratorRepository repository;
    private final AdministratorResponseMapper mapper;

    public ListAdministratorsUseCase(AdministratorRepository repository, AdministratorResponseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AdministratorResponseDTO> execute() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
}
