package com.sloth.OnlyStudent.application.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.shared.errors.EmailAlreadyExistsException;
import com.sloth.OnlyStudent.shared.errors.InvalidRegistrationException;
import com.sloth.OnlyStudent.repository.UserRepository;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void execute(RegisterDTO request) {
        if (request.role() != UserRole.STUDENT && request.role() != UserRole.EDUCATOR) {
            throw new InvalidRegistrationException("O cadastro está disponível apenas para alunos e educadores.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = request.role() == UserRole.EDUCATOR
                ? new Educator(null, request.name(), request.telephone(), request.email(), encodedPassword,
                        request.role(), request.especialidade())
                : new Student(null, request.name(), request.telephone(), request.email(), encodedPassword,
                        request.role());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException(exception);
        }
    }
}
