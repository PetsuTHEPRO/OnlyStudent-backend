package com.sloth.OnlyStudent.application.administrator;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;

@Service
public class CreateAdministratorUseCase {

    private final AdministratorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AdministratorResponseMapper mapper;

    public CreateAdministratorUseCase(AdministratorRepository repository, PasswordEncoder passwordEncoder,
            AdministratorResponseMapper mapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Transactional
    public AdministratorResponseDTO execute(RegisterDTO request) {
        Administrator administrator = new Administrator(null, request.name(), request.telephone(), request.email(),
                passwordEncoder.encode(request.password()), request.role());
        return mapper.toResponse(repository.save(administrator));
    }
}
