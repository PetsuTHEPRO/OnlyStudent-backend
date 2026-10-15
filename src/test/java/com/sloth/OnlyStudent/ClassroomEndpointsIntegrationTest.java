package com.sloth.OnlyStudent;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.MaterialType;
import com.sloth.OnlyStudent.entities.Status;
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
class ClassroomEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private EducatorRepository educatorRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private MaterialRepository materialRepository;

    private Educator owner;
    private Student student;
    private Educator otherEducator;

    @BeforeEach
    void setUp() {
        owner = educatorRepository.save(new Educator(null, "Dona", "111", "owner@example.com", "hash",
                UserRole.EDUCATOR, "Matemática"));
        student = studentRepository.save(new Student(null, "Aluno", "222", "student@example.com", "hash",
                UserRole.STUDENT));
        otherEducator = educatorRepository.save(new Educator(null, "Outro", "333", "other@example.com", "hash",
                UserRole.EDUCATOR, "História"));
    }

    @Test
    void educatorCreatesAClassroom() throws Exception {
        mockMvc.perform(post("/turma/register")
                        .with(as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(classroomRequest("Álgebra", 0, otherEducator.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string("Turma criada com sucesso!"));
    }

    @Test
    void studentCannotCreateAClassroom() throws Exception {
        mockMvc.perform(post("/turma/register")
                        .with(as(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(classroomRequest("Álgebra", 0, owner.getId())))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void duplicateClassroomNameReturnsConflictAsPlainText() throws Exception {
        classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(post("/turma/register")
                        .with(as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(classroomRequest("Álgebra", 0, owner.getId())))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Já existe uma turma com esse nome."));
    }

    @Test
    void studentJoinsAClassroom() throws Exception {
        Classroom classroom = classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(post("/turma/{codigo}/entrar", classroom.getCodigo())
                        .with(as(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idAluno\":\"99999\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Entrada confirmada com sucesso."));
    }

    @Test
    void educatorCannotJoinAClassroom() throws Exception {
        Classroom classroom = classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(post("/turma/{codigo}/entrar", classroom.getCodigo())
                        .with(as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idAluno\":\"1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void duplicateEnrollmentReturnsConflictAsPlainText() throws Exception {
        Classroom classroom = classroom("Álgebra", owner);
        classroom.matricular(student);
        classroom = classroomRepository.save(classroom);

        mockMvc.perform(post("/turma/{codigo}/entrar", classroom.getCodigo())
                        .with(as(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idAluno\":\"1\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Você já está matriculado nesta turma."));
    }

    @Test
    void ownerCanViewClassroomDetailsWithContractFields() throws Exception {
        Classroom classroom = classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(get("/turma/{id}", classroom.getCodigo()).with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(classroom.getCodigo()))
                .andExpect(jsonPath("$.name").value("Álgebra"))
                .andExpect(jsonPath("$.description").value("Descrição"))
                .andExpect(jsonPath("$.educator").value("Dona"))
                .andExpect(jsonPath("$.nomeAlunos").isArray())
                .andExpect(jsonPath("$.materiais").isArray());
    }

    @Test
    void enrolledStudentCanViewClassroomDetails() throws Exception {
        Classroom classroom = classroom("Álgebra", owner);
        classroom.matricular(student);
        classroom = classroomRepository.save(classroom);

        mockMvc.perform(get("/turma/{id}", classroom.getCodigo()).with(as(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(classroom.getCodigo()))
                .andExpect(jsonPath("$.nomeAlunos[0]").value("Aluno"));
    }

    @Test
    void unrelatedUserCannotViewClassroomDetails() throws Exception {
        Classroom classroom = classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(get("/turma/{id}", classroom.getCodigo()).with(as(otherEducator)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void studentWhoIsNotEnrolledCannotViewAnotherStudentsClassroom() throws Exception {
        Classroom classroom = classroom("Álgebra", owner);
        classroom.matricular(student);
        classroom = classroomRepository.save(classroom);
        Student otherStudent = studentRepository.save(new Student(null, "Outro aluno", "444",
                "other-student@example.com", "hash", UserRole.STUDENT));

        mockMvc.perform(get("/turma/{id}", classroom.getCodigo()).with(as(otherStudent)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void studentClassroomListIgnoresQueryIdentityAndKeepsPageShape() throws Exception {
        Classroom classroom = classroom("Álgebra", owner);
        classroom.matricular(student);
        classroomRepository.save(classroom);

        mockMvc.perform(get("/turma/studentTurmas")
                        .param("codigo", owner.getId().toString())
                        .param("page", "0")
                        .param("size", "50")
                        .with(as(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").exists())
                .andExpect(jsonPath("$.content[0].name").value("Álgebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Dona"))
                .andExpect(jsonPath("$.content[0].totalAlunos").value(1))
                .andExpect(jsonPath("$.content[0].totalMateriais").value(0));
    }

    @Test
    void educatorClassroomListIgnoresQueryIdentityAndKeepsPageShape() throws Exception {
        classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(get("/turma/educatorTurmas")
                        .param("codigo", otherEducator.getId().toString())
                        .param("page", "0")
                        .param("size", "50")
                        .with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").exists())
                .andExpect(jsonPath("$.content[0].name").value("Álgebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Dona"))
                .andExpect(jsonPath("$.content[0].status").value("ATIVA"))
                .andExpect(jsonPath("$.content[0].custo").value("GRATUITA"))
                .andExpect(jsonPath("$.content[0].price").value(0));
    }

    @Test
    void missingClassroomDetailsReturnsNotFoundAsPlainText() throws Exception {
        mockMvc.perform(get("/turma/99999").with(as(owner)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Turma não encontrada."));
    }

    @Test
    void joiningMissingClassroomReturnsNotFoundAsPlainText() throws Exception {
        mockMvc.perform(post("/turma/99999/entrar")
                        .with(as(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idAluno\":\"1\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Turma não encontrada."));
    }

    @Test
    void createIgnoresEducatorIdFromRequestAndUsesPrincipal() throws Exception {
        mockMvc.perform(post("/turma/register")
                        .with(as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(classroomRequest("Álgebra", 125, otherEducator.getId())))
                .andExpect(status().isOk());

        Classroom created = classroomRepository.findByName("Álgebra").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(owner.getId(), created.getEducator().getId());
        org.junit.jupiter.api.Assertions.assertEquals(125, created.getPrice());
        org.junit.jupiter.api.Assertions.assertEquals(Custo.PAGA, created.getCusto());
        org.junit.jupiter.api.Assertions.assertEquals(Status.ATIVA, created.getStatus());
    }

    @Test
    void catalogIsAvailableToAnyAuthenticatedRoleIncludingAdmin() throws Exception {
        mockMvc.perform(get("/turma/allClassroom").param("search", ""))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Autenticação necessária."));

        mockMvc.perform(get("/turma/allClassroom")
                        .param("search", "")
                        .with(as(new User(999L, "Admin", "123", "admin@example.com", "hash", UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void legacyReadRoutesRequireAuthenticationAndTurmasAlunoUsesPrincipal() throws Exception {
        Classroom classroom = classroom("Álgebra", owner);
        classroom.matricular(student);
        classroomRepository.save(classroom);

        mockMvc.perform(get("/turma").param("codigo", owner.getId().toString()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/turma/search").param("search", "Álgebra"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/turma/destaque"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/turma").param("codigo", owner.getId().toString()).with(as(student)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/turma/search").param("search", "Álgebra").with(as(student)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/turma/destaque").with(as(student)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/turma/turmasAluno")
                        .param("codigo", owner.getId().toString())
                        .with(as(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Álgebra"));

        mockMvc.perform(get("/turma/turmasAluno")
                        .param("codigo", student.getId().toString())
                        .with(as(owner)))
                .andExpect(status().isForbidden());
    }

    @Test
    void legacyStudentInfoAndMaterialsAreRestrictedToOwnerOrEnrolledStudent() throws Exception {
        Classroom classroom = classroom("Álgebra", owner);
        classroom.matricular(student);
        classroom = classroomRepository.save(classroom);
        Material material = materialRepository.save(new Material(null, "Apostila", MaterialType.PDF,
                "https://example.com/a.pdf", classroom));

        mockMvc.perform(get("/turma/{codigo}/students/info", classroom.getCodigo()).with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(student.getId()));
        mockMvc.perform(get("/turma/{codigo}/students/info", classroom.getCodigo()).with(as(student)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/turma/{codigo}/students/info", classroom.getCodigo()).with(as(otherEducator)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));

        mockMvc.perform(get("/turma/{codigo}/materials", classroom.getCodigo()).with(as(otherEducator)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
        mockMvc.perform(get("/turma/{codigo}/materials", classroom.getCodigo()).with(as(owner)))
                .andExpect(status().isOk());
    }

    @Test
    void legacyMutationsRequireTheOwningEducator() throws Exception {
        Classroom classroom = classroomRepository.save(classroom("Álgebra", owner));
        mockMvc.perform(post("/turma/register")
                        .with(as(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(classroomRequest("Nova", 0, owner.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/turma/{codigo}", classroom.getCodigo()).with(as(student)))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/turma/{codigo}", classroom.getCodigo()).with(as(otherEducator)))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/turma/{id}/status", classroom.getCodigo())
                        .with(as(otherEducator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOQUEADA\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/turma/{id}/status", classroom.getCodigo())
                        .with(as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOQUEADA\"}"))
                .andExpect(status().isOk());

        Material material = materialRepository.save(new Material(null, "Apostila", MaterialType.PDF,
                "https://example.com/a.pdf", classroom));
        classroom.getMaterials().add(material);
        classroomRepository.save(classroom);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/turma/{classroomId}/material/{materialId}", classroom.getCodigo(), material.getId())
                        .with(as(otherEducator)))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/turma/{classroomId}/material/{materialId}", classroom.getCodigo(), material.getId())
                        .with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string("Material removed from classroom successfully!"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/turma/{codigo}", classroom.getCodigo()).with(as(owner)))
                .andExpect(status().isNoContent());
    }

    @Test
    void educatorCannotDeleteAnotherEducatorsClassroom() throws Exception {
        Classroom classroom = classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/turma/{codigo}", classroom.getCodigo()).with(as(otherEducator)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void educatorCannotUpdateAnotherEducatorsClassroomStatus() throws Exception {
        Classroom classroom = classroomRepository.save(classroom("Álgebra", owner));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/turma/{id}/status", classroom.getCodigo())
                        .with(as(otherEducator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOQUEADA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    private Classroom classroom(String name, Educator educator) {
        return Classroom.criar(name, "Descrição", educator, 0);
    }

    private String classroomRequest(String name, double price, Long educatorId) {
        return """
                {"name":"%s","description":"Descrição","idEducator":%d,"price":%s}
                """.formatted(name, educatorId, price);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor as(User principal) {
        return authentication(new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRoles().name()))));
    }
}
