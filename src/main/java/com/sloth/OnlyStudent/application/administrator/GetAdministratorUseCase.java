package com.sloth.OnlyStudent.application.administrator;

import java.util.Optional;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;

@Service
public class GetAdministratorUseCase {

    private final AdministratorRepository repository;
    private final AdministratorResponseMapper mapper;

    public GetAdministratorUseCase(AdministratorRepository repository, AdministratorResponseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Optional<AdministratorResponseDTO> execute(Long id) {
        return repository.findById(Objects.requireNonNull(id, "id")).map(mapper::toResponse);
    }
}
