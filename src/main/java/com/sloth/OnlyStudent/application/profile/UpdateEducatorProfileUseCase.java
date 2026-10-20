package com.sloth.OnlyStudent.application.profile;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.entities.DTO.EducatorResponseDTO;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

@Service
public class UpdateEducatorProfileUseCase {

    private final EducatorRepository educatorRepository;
    private final EducatorProfileMapper mapper;

    public UpdateEducatorProfileUseCase(EducatorRepository educatorRepository, EducatorProfileMapper mapper) {
        this.educatorRepository = educatorRepository;
        this.mapper = mapper;
    }

    @Transactional
    public Optional<EducatorResponseDTO> execute(Long principalId, String requestedLogin, EducatorDTO request) {
        Optional<Educator> optionalEducator = educatorRepository.findById(principalId);
        if (optionalEducator.isEmpty()) {
            return Optional.empty();
        }

        Educator educator = optionalEducator.get();
        if (requestedLogin != null && !requestedLogin.equals(educator.getEmail())) {
            throw new ProfileAccessDeniedException();
        }
        educator.setName(request.name());
        educator.setTelephone(request.telefone());
        educator.setEspecialidade(request.especialidade());
        return Optional.of(mapper.toResponse(educatorRepository.save(educator)));
    }
}
