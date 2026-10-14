package com.sloth.OnlyStudent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sloth.OnlyStudent.controller.AuthenticationController;
import com.sloth.OnlyStudent.controller.ClassroomController;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.MaterialType;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.entities.DTO.ResponseDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.services.AuthenticationService;
import com.sloth.OnlyStudent.services.ClassroomService;
import com.sloth.OnlyStudent.shared.errors.EmailAlreadyExistsException;
import com.sloth.OnlyStudent.shared.errors.GlobalErrorHandler;

@ExtendWith(MockitoExtension.class)
class ApiContractCharacterizationTest {

    private MockMvc mockMvc;
    private AuthenticationController authenticationController;
    private ClassroomController classroomController;

    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final ClassroomService classroomService = mock(ClassroomService.class);
    private final ClassroomRepository classroomRepository = mock(ClassroomRepository.class);
    private final EducatorRepository educatorRepository = mock(EducatorRepository.class);
    private final StudentRepository studentRepository = mock(StudentRepository.class);
    private final MaterialRepository materialRepository = mock(MaterialRepository.class);

    @BeforeEach
    void setUp() {
        authenticationController = new AuthenticationController(authenticationService);

        classroomController = new ClassroomController();
        ReflectionTestUtils.setField(classroomController, "classroomRepository", classroomRepository);
        ReflectionTestUtils.setField(classroomController, "classroomService", classroomService);
        ReflectionTestUtils.setField(classroomController, "educatorRepository", educatorRepository);
        ReflectionTestUtils.setField(classroomController, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(classroomController, "materialRepository", materialRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController, classroomController)
                .setControllerAdvice(new GlobalErrorHandler())
                .build();
    }

    @Test
    void authRegisterReturnsCreatedWithoutToken() throws Exception {
        when(authenticationService.createUser(any())).thenReturn(
                org.springframework.http.ResponseEntity.status(201).build());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"student@example.com","telephone":"123","name":"Ana",
                                 "password":"secret1","role":"STUDENT","especialidade":null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void authLoginKeepsDirectJsonAndLowercaseRole() throws Exception {
        doReturn(org.springframework.http.ResponseEntity.ok(new ResponseDTO(
                "Ana", "student@example.com", "student", "123", null, "jwt-login-token", 7L)))
                .when(authenticationService).authenticateUser(any(AuthenticationDTO.class));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"student@example.com","password":"secret1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andExpect(jsonPath("$.role").value("student"))
                .andExpect(jsonPath("$.telephone").value("123"))
                .andExpect(jsonPath("$.especialidade").doesNotExist())
                .andExpect(jsonPath("$.token").value("jwt-login-token"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void authLoginErrorRemainsPlainText() throws Exception {
        doReturn(org.springframework.http.ResponseEntity.badRequest().body("Senha incorreta."))
                .when(authenticationService).authenticateUser(any(AuthenticationDTO.class));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"student@example.com","password":"wrong12"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Senha incorreta."));
    }

    @Test
    void authRegisterConflictRemainsPlainText() throws Exception {
        org.mockito.Mockito.doThrow(new EmailAlreadyExistsException())
                .when(authenticationService).createUser(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"student@example.com","telephone":"123","name":"Ana",
                                 "password":"secret1","role":"STUDENT","especialidade":null}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email já cadastrado."));
    }

    @Test
    void classroomRegisterKeepsSuccessTextResponse() throws Exception {
        Educator educator = new Educator(3L, "Edu", "123", "edu@example.com", "hash", UserRole.EDUCATOR, "Math");
        when(classroomRepository.existsByName("Algebra")).thenReturn(false);
        when(educatorRepository.findById(3L)).thenReturn(Optional.of(educator));
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/turma/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Algebra","description":"Aula","idEducator":3,"price":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Turma criada com sucesso!"));
    }

    @Test
    void classroomRegisterConflictRemainsPlainText() throws Exception {
        when(classroomRepository.existsByName("Algebra")).thenReturn(true);

        mockMvc.perform(post("/turma/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Algebra","description":"Aula","idEducator":3,"price":0}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("Já existe uma turma com esse nome."));
    }

    @Test
    void studentTurmasKeepsPageContentShape() throws Exception {
        when(classroomService.getClassroomsByStudentId(anyLong(), any()))
                .thenReturn(pageOfClassrooms());

        mockMvc.perform(get("/turma/studentTurmas").param("codigo", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(12))
                .andExpect(jsonPath("$.content[0].name").value("Algebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Edu"))
                .andExpect(jsonPath("$.content[0].totalAlunos").value(4))
                .andExpect(jsonPath("$.content[0].totalMateriais").value(2));
        verify(classroomService).getClassroomsByStudentId(7L, PageRequest.of(0, 50));
    }

    @Test
    void educatorTurmasKeepsPageContentShape() throws Exception {
        when(classroomService.getClassroomsByEducatorId(anyLong(), any()))
                .thenReturn(pageOfClassrooms());

        mockMvc.perform(get("/turma/educatorTurmas").param("codigo", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(12))
                .andExpect(jsonPath("$.content[0].name").value("Algebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Edu"))
                .andExpect(jsonPath("$.content[0].totalAlunos").value(4))
                .andExpect(jsonPath("$.content[0].totalMateriais").value(2));
        verify(classroomService).getClassroomsByEducatorId(3L, PageRequest.of(0, 50));
    }

    @Test
    void allClassroomKeepsPageContentShape() throws Exception {
        when(classroomService.getAllClassrooms(org.mockito.ArgumentMatchers.eq(""), any()))
                .thenReturn(pageOfClassrooms());

        mockMvc.perform(get("/turma/allClassroom").param("search", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(12))
                .andExpect(jsonPath("$.content[0].name").value("Algebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Edu"))
                .andExpect(jsonPath("$.content[0].totalAlunos").value(4));
    }

    @Test
    void classroomDetailsKeepsDirectJsonShape() throws Exception {
        Educator educator = new Educator(3L, "Edu", "123", "edu@example.com", "hash", UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom(12L, "Algebra", "Aula", Status.ATIVA, Custo.GRATUITA,
                0, new HashSet<>(List.of(new Student(7L, "Ana", "456", "ana@example.com", "hash", UserRole.STUDENT))),
                educator, new HashSet<>(List.of(new Material(9L, "PDF", MaterialType.PDF,
                        "https://example.com/a.pdf", null))));
        when(classroomRepository.findById(12L)).thenReturn(Optional.of(classroom));

        mockMvc.perform(get("/turma/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Algebra"))
                .andExpect(jsonPath("$.description").value("Aula"))
                .andExpect(jsonPath("$.educator").value("Edu"))
                .andExpect(jsonPath("$.nomeAlunos[0]").value("Ana"))
                .andExpect(jsonPath("$.materiais[0].id").value(9))
                .andExpect(jsonPath("$.materiais[0].name").value("PDF"))
                .andExpect(jsonPath("$.materiais[0].tipo").value("PDF"))
                .andExpect(jsonPath("$.materiais[0].url").value("https://example.com/a.pdf"));
    }

    @Test
    void classroomJoinKeepsSuccessTextResponse() throws Exception {
        when(classroomService.addStudentInClassroom(12L, 7L)).thenReturn(true);

        mockMvc.perform(post("/turma/12/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idAluno":"7"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Entrada confirmada com sucesso."));
    }

    @Test
    void classroomJoinFailureRemainsPlainText() throws Exception {
        when(classroomService.addStudentInClassroom(12L, 7L)).thenReturn(false);

        mockMvc.perform(post("/turma/12/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idAluno":"7"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro ao confirmar a entrada."));
    }

    private PageImpl<ClassroomsDTO> pageOfClassrooms() {
        ClassroomsDTO classroom = new ClassroomsDTO(12L, "Algebra", "Edu", Status.ATIVA,
                Custo.GRATUITA, 0, 4, 2);
        return new PageImpl<>(List.of(classroom), PageRequest.of(0, 50), 1);
    }
}
