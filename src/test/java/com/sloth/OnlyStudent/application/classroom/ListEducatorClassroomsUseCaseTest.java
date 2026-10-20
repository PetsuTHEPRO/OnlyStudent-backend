package com.sloth.OnlyStudent.application.classroom;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.sloth.OnlyStudent.repository.ClassroomRepository;

@ExtendWith(MockitoExtension.class)
class ListEducatorClassroomsUseCaseTest {

    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private ClassroomResponseMapper mapper;
    @InjectMocks
    private ListEducatorClassroomsUseCase useCase;

    @Test
    void listsByPrincipalIdAndRequestedPageable() {
        var pageable = PageRequest.of(1, 50);
        when(classroomRepository.findByEducatorId(3L, pageable)).thenReturn(new PageImpl<>(List.of()));
        useCase.execute(3L, pageable);
        verify(classroomRepository).findByEducatorId(3L, pageable);
    }
}
