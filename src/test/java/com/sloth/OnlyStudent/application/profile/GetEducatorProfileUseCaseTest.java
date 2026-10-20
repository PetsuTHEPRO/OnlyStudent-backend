package com.sloth.OnlyStudent.application.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

class GetEducatorProfileUseCaseTest {

    private final EducatorRepository educatorRepository = mock(EducatorRepository.class);
    private final GetEducatorProfileUseCase useCase =
            new GetEducatorProfileUseCase(educatorRepository, new EducatorProfileMapper());

    @Test
    void returnsProfileForAuthenticatedEducatorId() {
        when(educatorRepository.findById(9L)).thenReturn(Optional.of(educator(9L)));

        var response = useCase.execute(9L, 9L).orElseThrow();

        assertEquals("Edu", response.name());
        assertEquals("Math", response.especialidade());
        verify(educatorRepository).findById(9L);
    }

    @Test
    void rejectsARequestedIdDifferentFromAuthenticatedEducator() {
        assertThrows(ProfileAccessDeniedException.class, () -> useCase.execute(9L, 10L));
        verifyNoInteractions(educatorRepository);
    }

    private Educator educator(Long id) {
        return new Educator(id, "Edu", "111", "edu@example.com", "hash", UserRole.EDUCATOR, "Math");
    }
}
