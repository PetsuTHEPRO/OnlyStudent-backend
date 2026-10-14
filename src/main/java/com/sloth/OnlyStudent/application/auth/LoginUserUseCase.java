package com.sloth.OnlyStudent.application.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.entities.DTO.ResponseDTO;
import com.sloth.OnlyStudent.infra.security.TokenService;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.InvalidCredentialsException;
import com.sloth.OnlyStudent.shared.errors.UserNotFoundException;

@Service
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public ResponseDTO execute(AuthenticationDTO request) {
        User user = userRepository.findByEmail(request.email());
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String specialty = user instanceof Educator educator ? educator.getEspecialidade() : null;
        String token = tokenService.generateToken(user);
        return new ResponseDTO(user.getName(), user.getEmail(), user.getRoles().getRole(),
                user.getTelephone(), specialty, token, user.getId());
    }
}
