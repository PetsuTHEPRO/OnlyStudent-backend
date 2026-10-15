package com.sloth.OnlyStudent.application.profile;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.DTO.EducatorResponseDTO;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

@Service
public class GetEducatorProfileUseCase {

    private final EducatorRepository educatorRepository;
    private final EducatorProfileMapper mapper;

    public GetEducatorProfileUseCase(EducatorRepository educatorRepository, EducatorProfileMapper mapper) {
        this.educatorRepository = educatorRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Optional<EducatorResponseDTO> execute(Long principalId, Long requestedId) {
        if (requestedId != null && !Objects.equals(requestedId, principalId)) {
            throw new ProfileAccessDeniedException();
        }

        return educatorRepository.findById(principalId).map(mapper::toResponse);
    }
}
