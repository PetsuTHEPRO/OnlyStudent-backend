package com.sloth.OnlyStudent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.sloth.OnlyStudent.controller.AdministratorController;
import com.sloth.OnlyStudent.controller.EducatorController;
import com.sloth.OnlyStudent.controller.MaterialController;
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

class CriticalCorrectionsTest {

    @Test
    void materialRegistrationRequiresClassroomOwner() {
        MaterialRepository materials = mock(MaterialRepository.class);
        ClassroomRepository classrooms = mock(ClassroomRepository.class);
        MaterialController controller = new MaterialController();
        ReflectionTestUtils.setField(controller, "materialRepository", materials);
        ReflectionTestUtils.setField(controller, "classroomRepository", classrooms);

        Educator educator = new Educator(14L, "Edu", "123", "edu@example.com", "hash",
                UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom();
        classroom.setEducator(educator);
        when(classrooms.findByCodigo(22L)).thenReturn(Optional.of(classroom));
        when(materials.existsByNameAndTurmaCodigo("Apostila", 22L)).thenReturn(false);

        assertEquals(200, controller.registerMaterial(
                new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 22L),
                user(14L, UserRole.EDUCATOR)).getStatusCode().value());
        verify(materials).save(org.mockito.ArgumentMatchers.any(Material.class));

        assertEquals(403, controller.registerMaterial(
                new MaterialDTO("Apostila", "pdf", "https://example.com/a.pdf", 22L),
                user(15L, UserRole.EDUCATOR)).getStatusCode().value());
    }

    @Test
    void materialDeleteUsesRequestedIdAndRequiresItsOwner() {
        MaterialRepository materials = mock(MaterialRepository.class);
        MaterialController controller = new MaterialController();
        ReflectionTestUtils.setField(controller, "materialRepository", materials);

        User owner = user(14L, UserRole.EDUCATOR);
        Educator educator = new Educator(14L, "Edu", "123", "edu@example.com", "hash",
                UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom();
        classroom.setEducator(educator);
        Material material = new Material(91L, "Apostila", MaterialType.PDF, "https://example.com/a.pdf",
                classroom);
        when(materials.findById(91L)).thenReturn(Optional.of(material));

        assertEquals("Material deleted successfully.",
                controller.deleteMaterial(91L, owner).getBody());
        verify(materials).deleteById(91L);
    }

    @Test
    void materialDeleteRejectsNonOwner() {
        MaterialRepository materials = mock(MaterialRepository.class);
        MaterialController controller = new MaterialController();
        ReflectionTestUtils.setField(controller, "materialRepository", materials);

        Educator educator = new Educator(14L, "Edu", "123", "edu@example.com", "hash",
                UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom();
        classroom.setEducator(educator);
        when(materials.findById(91L)).thenReturn(Optional.of(
                new Material(91L, "Apostila", MaterialType.PDF, "https://example.com/a.pdf", classroom)));

        assertEquals(403, controller.deleteMaterial(91L, user(15L, UserRole.EDUCATOR))
                .getStatusCode().value());
    }

    @Test
    void studentProfileUsesAuthenticatedIdInsteadOfQueryId() {
        StudentRepository students = mock(StudentRepository.class);
        StudentController controller = new StudentController();
        ReflectionTestUtils.setField(controller, "studentRepository", students);
        when(students.findById(14L)).thenReturn(Optional.of(
                new Student(14L, "Ana", "123", "ana@example.com", "hash", UserRole.STUDENT)));

        assertEquals("ana@example.com", controller.getUser(999L, user(14L, UserRole.STUDENT))
                .getBody().login());
        verify(students).findById(14L);
    }

    @Test
    void educatorProfileUsesAuthenticatedIdAndEmailLookupReturnsPrincipalId() {
        EducatorRepository educators = mock(EducatorRepository.class);
        EducatorController controller = new EducatorController();
        ReflectionTestUtils.setField(controller, "educatorRepository", educators);
        User principal = user(14L, UserRole.EDUCATOR);
        when(educators.findById(14L)).thenReturn(Optional.of(
                new Educator(14L, "Edu", "123", "edu@example.com", "hash", UserRole.EDUCATOR, "Math")));

        assertEquals(14L, controller.getEducatorIdByEmail("someone-else@example.com", principal).getBody());
        assertEquals("edu@example.com", controller.getUser(999L, principal).getBody().login());
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
