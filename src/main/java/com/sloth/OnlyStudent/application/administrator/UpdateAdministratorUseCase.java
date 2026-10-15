package com.sloth.OnlyStudent.application.administrator;

import java.util.Optional;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;

@Service
public class UpdateAdministratorUseCase {

    private final AdministratorRepository repository;
    private final AdministratorResponseMapper mapper;

    public UpdateAdministratorUseCase(AdministratorRepository repository, AdministratorResponseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Optional<AdministratorResponseDTO> execute(Long id, RegisterDTO request) {
        Optional<Administrator> optionalAdministrator = repository.findById(Objects.requireNonNull(id, "id"));
        if (optionalAdministrator.isEmpty()) {
            return Optional.empty();
        }

        Administrator administrator = optionalAdministrator.get();
        administrator.setName(request.name());
        administrator.setEmail(request.email());
        return Optional.of(mapper.toResponse(repository.save(administrator)));
    }
}
