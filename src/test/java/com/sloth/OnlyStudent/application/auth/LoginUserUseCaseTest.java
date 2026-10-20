package com.sloth.OnlyStudent.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.infra.security.TokenService;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.InvalidCredentialsException;
import com.sloth.OnlyStudent.shared.errors.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LoginUserUseCase useCase;

    @Test
    void authenticatesStudentAndReturnsLowercaseRole() {
        User user = new Student(7L, "Ana", "123", "ana@example.com", "encoded", UserRole.STUDENT);
        when(userRepository.findByEmail("ana@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret1", "encoded")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("jwt");

        var response = useCase.execute(new AuthenticationDTO("ana@example.com", "secret1"));

        assertEquals("student", response.role());
        assertEquals("jwt", response.token());
        assertNull(response.especialidade());
    }

    @Test
    void throwsTypedErrorWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(null);

        assertThrows(UserNotFoundException.class,
                () -> useCase.execute(new AuthenticationDTO("missing@example.com", "secret1")));
        verify(passwordEncoder, never()).matches(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void throwsTypedErrorWhenPasswordIsIncorrect() {
        User user = new Student(7L, "Ana", "123", "ana@example.com", "encoded", UserRole.STUDENT);
        when(userRepository.findByEmail("ana@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong1", "encoded")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new AuthenticationDTO("ana@example.com", "wrong1")));
        verify(tokenService, never()).generateToken(user);
    }
}
