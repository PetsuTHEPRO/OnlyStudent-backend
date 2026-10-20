package com.sloth.OnlyStudent.application.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

class UpdateEducatorProfileUseCaseTest {

    private final EducatorRepository educatorRepository = mock(EducatorRepository.class);
    private final UpdateEducatorProfileUseCase useCase =
            new UpdateEducatorProfileUseCase(educatorRepository, new EducatorProfileMapper());

    @Test
    void updatesOnlyTheAuthenticatedEducatorProfile() {
        Educator educator = educator();
        when(educatorRepository.findById(9L)).thenReturn(Optional.of(educator));
        when(educatorRepository.save(any(Educator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(9L, "edu@example.com",
                new EducatorDTO("Edu Novo", "222", "ignored@example.com", "Science"));

        assertEquals("Edu Novo", response.orElseThrow().name());
        assertEquals("222", educator.getTelephone());
        assertEquals("Science", educator.getEspecialidade());
        assertEquals("edu@example.com", educator.getEmail());
        verify(educatorRepository).save(educator);
    }

    @Test
    void rejectsAnUpdateIdentifyingAnotherLogin() {
        when(educatorRepository.findById(9L)).thenReturn(Optional.of(educator()));

        assertThrows(ProfileAccessDeniedException.class,
                () -> useCase.execute(9L, "other@example.com",
                        new EducatorDTO("Intruso", "222", "other@example.com", "Science")));
        verify(educatorRepository).findById(9L);
        verifyNoMoreInteractions(educatorRepository);
    }

    private Educator educator() {
        return new Educator(9L, "Edu", "111", "edu@example.com", "hash", UserRole.EDUCATOR, "Math");
    }
}
