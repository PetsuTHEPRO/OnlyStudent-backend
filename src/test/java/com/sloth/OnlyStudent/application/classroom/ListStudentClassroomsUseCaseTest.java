package com.sloth.OnlyStudent.application.classroom;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import com.sloth.OnlyStudent.repository.ClassroomRepository;

@ExtendWith(MockitoExtension.class)
class ListStudentClassroomsUseCaseTest {

    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private ClassroomResponseMapper mapper;
    @InjectMocks
    private ListStudentClassroomsUseCase useCase;

    @Test
    void listsByPrincipalIdAndRequestedPageable() {
        var pageable = PageRequest.of(2, 50);
        when(classroomRepository.findByStudentId(7L, pageable)).thenReturn(new PageImpl<>(List.of()));
        useCase.execute(7L, pageable);
        verify(classroomRepository).findByStudentId(7L, pageable);
    }
}
