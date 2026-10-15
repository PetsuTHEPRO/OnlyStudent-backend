package com.sloth.OnlyStudent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sloth.OnlyStudent.application.material.CreateMaterialUseCase;
import com.sloth.OnlyStudent.application.material.DeleteMaterialUseCase;
import com.sloth.OnlyStudent.application.profile.EducatorProfileMapper;
import com.sloth.OnlyStudent.application.profile.GetEducatorProfileUseCase;
import com.sloth.OnlyStudent.application.profile.GetStudentProfileUseCase;
import com.sloth.OnlyStudent.application.profile.StudentProfileMapper;
import com.sloth.OnlyStudent.application.profile.UpdateEducatorProfileUseCase;
import com.sloth.OnlyStudent.controller.AdministratorController;
import com.sloth.OnlyStudent.controller.EducatorController;
import com.sloth.OnlyStudent.controller.StudentController;
import com.sloth.OnlyStudent.controller.SupportController;
import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.MaterialType;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;
import com.sloth.OnlyStudent.entities.DTO.SupportDTO;
import com.sloth.OnlyStudent.infra.security.SecurityConfigurations;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.MaterialAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

class CriticalCorrectionsTest {

    @Test
    void materialRegistrationRequiresClassroomOwner() {
        MaterialRepository materials = mock(MaterialRepository.class);
        ClassroomRepository classrooms = mock(ClassroomRepository.class);
        CreateMaterialUseCase useCase = new CreateMaterialUseCase(classrooms, materials);

        Educator educator = new Educator(14L, "Edu", "123", "edu@example.com", "hash",
                UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom();
        classroom.setEducator(educator);
        when(classrooms.findByCodigo(22L)).thenReturn(Optional.of(classroom));
        when(materials.existsByNameAndTurmaCodigo("Apostila", 22L)).thenReturn(false);

        useCase.execute(new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 22L),
                14L, UserRole.EDUCATOR);
        verify(materials).save(org.mockito.ArgumentMatchers.any(Material.class));

        assertThrows(MaterialAccessDeniedException.class, () -> useCase.execute(
                new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 22L),
                15L, UserRole.EDUCATOR));
    }

    @Test
    void materialDeleteUsesRequestedIdAndRequiresItsOwner() {
        MaterialRepository materials = mock(MaterialRepository.class);
        DeleteMaterialUseCase useCase = new DeleteMaterialUseCase(materials);

        User owner = user(14L, UserRole.EDUCATOR);
        Educator educator = new Educator(14L, "Edu", "123", "edu@example.com", "hash",
                UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom();
        classroom.setEducator(educator);
        Material material = new Material(91L, "Apostila", MaterialType.PDF, "https://example.com/a.pdf",
                classroom);
        when(materials.findById(91L)).thenReturn(Optional.of(material));

        useCase.execute(91L, owner.getId(), owner.getRoles());
        verify(materials).delete(material);
    }

    @Test
    void materialDeleteRejectsNonOwner() {
        MaterialRepository materials = mock(MaterialRepository.class);
        DeleteMaterialUseCase useCase = new DeleteMaterialUseCase(materials);

        Educator educator = new Educator(14L, "Edu", "123", "edu@example.com", "hash",
                UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom();
        classroom.setEducator(educator);
        when(materials.findById(91L)).thenReturn(Optional.of(
                new Material(91L, "Apostila", MaterialType.PDF, "https://example.com/a.pdf", classroom)));

        assertThrows(MaterialAccessDeniedException.class,
                () -> useCase.execute(91L, 15L, UserRole.EDUCATOR));
    }

    @Test
    void studentProfileUsesAuthenticatedIdInsteadOfQueryId() {
        StudentRepository students = mock(StudentRepository.class);
        GetStudentProfileUseCase useCase = new GetStudentProfileUseCase(students, new StudentProfileMapper());
        when(students.findById(14L)).thenReturn(Optional.of(
                new Student(14L, "Ana", "123", "ana@example.com", "hash", UserRole.STUDENT)));

        assertThrows(ProfileAccessDeniedException.class, () -> useCase.execute(14L, 999L));
        assertEquals("ana@example.com", useCase.execute(14L, null).orElseThrow().login());
        verify(students).findById(14L);
    }

    @Test
    void educatorProfileUsesAuthenticatedIdAndEmailLookupReturnsPrincipalId() {
        EducatorRepository educators = mock(EducatorRepository.class);
        GetEducatorProfileUseCase getProfile = new GetEducatorProfileUseCase(educators,
                new EducatorProfileMapper());
        EducatorController controller = new EducatorController(getProfile,
                mock(UpdateEducatorProfileUseCase.class));
        User principal = user(14L, UserRole.EDUCATOR);
        when(educators.findById(14L)).thenReturn(Optional.of(
                new Educator(14L, "Edu", "123", "edu@example.com", "hash", UserRole.EDUCATOR, "Math")));

        assertEquals(14L, controller.getEducatorIdByEmail("someone-else@example.com", principal).getBody());
        assertThrows(ProfileAccessDeniedException.class, () -> getProfile.execute(14L, 999L));
        assertEquals("edu@example.com", getProfile.execute(14L, null).orElseThrow().login());
        verify(educators).findById(14L);
    }

    @Test
    void adminEndpointsAreGuardedByMethodSecurity() {
        assertNotNull(SecurityConfigurations.class.getAnnotation(EnableMethodSecurity.class));
        PreAuthorize authorization = AdministratorController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(authorization);
        assertEquals("hasRole('ADMIN')", authorization.value());
    }

    @Test
    void administratorJsonDoesNotContainPassword() throws Exception {
        String json = new ObjectMapper().writeValueAsString(
                new Administrator(1L, "Admin", "555", "admin@example.com", "secret-hash", UserRole.ADMIN));

        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret-hash"));
    }

    @Test
    void supportMessageEscapesUserProvidedHtml() {
        SupportController controller = new SupportController();
        SupportDTO request = new SupportDTO("<script>alert(1)</script>", "a&b@example.com", "555",
                "<b>motivo</b>", "<img src=x onerror=alert(1)>");

        String html = ReflectionTestUtils.invokeMethod(controller, "generateHtmlContent", request);

        assertNotNull(html);
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertTrue(html.contains("&lt;img src=x onerror=alert(1)&gt;"));
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<img"));
    }

    private User user(Long id, UserRole role) {
        return new User(id, "User", "123", "user@example.com", "hash", role);
    }
}
