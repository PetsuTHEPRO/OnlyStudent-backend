package com.sloth.OnlyStudent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.MaterialType;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MaterialEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private EducatorRepository educatorRepository;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private EntityManager entityManager;

    private Educator owner;
    private Educator otherEducator;
    private Student student;
    private Student otherStudent;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        owner = educatorRepository.save(new Educator(null, "Dona", "111", "material-owner@example.com",
                "hash", UserRole.EDUCATOR, "Matemática"));
        otherEducator = educatorRepository.save(new Educator(null, "Outro", "222", "material-other@example.com",
                "hash", UserRole.EDUCATOR, "História"));
        student = studentRepository.save(new Student(null, "Matriculado", "333", "material-student@example.com",
                "hash", UserRole.STUDENT));
        otherStudent = studentRepository.save(new Student(null, "Visitante", "444",
                "material-visitor@example.com", "hash", UserRole.STUDENT));
        classroom = Classroom.criar("Turma de materiais", "Descrição", owner, 0);
        classroom.matricular(student);
        classroom = classroomRepository.save(classroom);
    }

    @Test
    void educatorOwnerCreatesMaterialWithPlainTextSuccess() throws Exception {
        mockMvc.perform(post("/material/register")
                        .with(as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialRequest(classroom.getCodigo(), "Apostila")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Material registered successfully!"));
    }

    @Test
    void nonOwnerEducatorCannotCreateMaterial() throws Exception {
        mockMvc.perform(post("/material/register")
                        .with(as(otherEducator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialRequest(classroom.getCodigo(), "Apostila")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Apenas o educador responsável pode alterar esta turma."));
    }

    @Test
    void studentCannotCreateMaterial() throws Exception {
        mockMvc.perform(post("/material/register")
                        .with(as(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialRequest(classroom.getCodigo(), "Apostila")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void educatorOwnerDeletesMaterialWithPlainTextSuccess() throws Exception {
        Material material = saveMaterial("Apostila");

        mockMvc.perform(delete("/material/delete/{id}", material.getId()).with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Material deleted successfully."));
    }

    @Test
    void nonOwnerEducatorCannotDeleteMaterial() throws Exception {
        Material material = saveMaterial("Apostila");

        mockMvc.perform(delete("/material/delete/{id}", material.getId()).with(as(otherEducator)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Apenas o educador responsável pode alterar esta turma."));
    }

    @Test
    void studentCannotDeleteMaterial() throws Exception {
        Material material = saveMaterial("Apostila");

        mockMvc.perform(delete("/material/delete/{id}", material.getId()).with(as(student)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void ownerListsMaterialsAsTheContractDtoArray() throws Exception {
        Material material = saveMaterial("Apostila");

        mockMvc.perform(get("/turma/{codigo}/materials", classroom.getCodigo()).with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(material.getId()))
                .andExpect(jsonPath("$[0].tipo").value("PDF"))
                .andExpect(jsonPath("$[0].name").value("Apostila"))
                .andExpect(jsonPath("$[0].url").value("https://example.com/material.pdf"))
                .andExpect(jsonPath("$[0].turma").doesNotExist());
    }

    @Test
    void enrolledStudentListsMaterials() throws Exception {
        Material material = saveMaterial("Apostila");

        mockMvc.perform(get("/turma/{codigo}/materials", classroom.getCodigo()).with(as(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(material.getId()))
                .andExpect(jsonPath("$[0].name").value("Apostila"));
    }

    @Test
    void nonEnrolledStudentCannotListMaterials() throws Exception {
        mockMvc.perform(get("/turma/{codigo}/materials", classroom.getCodigo()).with(as(otherStudent)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void listingSeveralMaterialsUsesBoundedQueriesAndNoPerMaterialLoads() throws Exception {
        saveMaterial("Material 1");
        saveMaterial("Material 2");
        saveMaterial("Material 3");
        entityManager.flush();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get("/turma/{codigo}/materials", classroom.getCodigo()).with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].turma").doesNotExist());

        long preparedStatements = statistics.getPrepareStatementCount();
        assertTrue(preparedStatements <= 4,
                "Expected a bounded query count for three materials, but got " + preparedStatements);
    }

    private Material saveMaterial(String name) {
        return materialRepository.save(Material.criar("PDF", name,
                "https://example.com/material.pdf", classroom));
    }

    private String materialRequest(Long classroomCode, String name) {
        return """
                {"name":"%s","tipo":"pdf","url":"https://example.com/material.pdf","turmaCodigo":%d}
                """.formatted(name, classroomCode);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor as(User principal) {
        return authentication(new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRoles().name()))));
    }
}
