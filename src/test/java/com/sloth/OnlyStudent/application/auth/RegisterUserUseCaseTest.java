package com.sloth.OnlyStudent.application.auth;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.RegisterDTO;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.EmailAlreadyExistsException;
import com.sloth.OnlyStudent.shared.errors.InvalidRegistrationException;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase useCase;

    @Test
    void registersStudentWithEncodedPassword() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");

        useCase.execute(request(UserRole.STUDENT, null));

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(user.capture());
        assertInstanceOf(Student.class, user.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("encoded", user.getValue().getPassword());
    }

    @Test
    void registersEducatorWithSpecialty() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");

        useCase.execute(request(UserRole.EDUCATOR, "Matemática"));

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(user.capture());
        Educator educator = assertInstanceOf(Educator.class, user.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("Matemática", educator.getEspecialidade());
    }

    @Test
    void rejectsAdminRegistration() {
        assertThrows(InvalidRegistrationException.class,
                () -> useCase.execute(request(UserRole.ADMIN, null)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsExistingEmail() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> useCase.execute(request(UserRole.STUDENT, null)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void mapsConcurrentDuplicateEmailConstraintToDomainError() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(EmailAlreadyExistsException.class,
                () -> useCase.execute(request(UserRole.STUDENT, null)));
    }

    private RegisterDTO request(UserRole role, String specialty) {
        return new RegisterDTO("ana@example.com", "123", "Ana", "secret1", role, specialty);
    }
}
