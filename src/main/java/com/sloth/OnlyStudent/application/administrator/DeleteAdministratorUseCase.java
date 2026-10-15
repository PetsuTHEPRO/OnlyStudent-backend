package com.sloth.OnlyStudent.application.administrator;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.repository.AdministratorRepository;

@Service
public class DeleteAdministratorUseCase {

    private final AdministratorRepository repository;

    public DeleteAdministratorUseCase(AdministratorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean execute(Long id) {
        return repository.findById(Objects.requireNonNull(id, "id"))
                .map(administrator -> {
                    repository.delete(Objects.requireNonNull(administrator, "administrator"));
                    return true;
                })
                .orElse(false);
    }
}
