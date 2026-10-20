package com.sloth.OnlyStudent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.sloth.OnlyStudent.application.classroom.CreateClassroomUseCase;
import com.sloth.OnlyStudent.application.classroom.GetClassroomDetailsUseCase;
import com.sloth.OnlyStudent.application.classroom.JoinClassroomUseCase;
import com.sloth.OnlyStudent.application.classroom.ListEducatorClassroomsUseCase;
import com.sloth.OnlyStudent.application.classroom.ListStudentClassroomsUseCase;
import com.sloth.OnlyStudent.application.classroom.SearchClassroomsUseCase;
import com.sloth.OnlyStudent.application.material.ListMaterialsUseCase;
import com.sloth.OnlyStudent.controller.AuthenticationController;
import com.sloth.OnlyStudent.controller.ClassroomController;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.AuthenticationDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDetailsDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDTO;
import com.sloth.OnlyStudent.entities.DTO.ClassroomsDTO;
import com.sloth.OnlyStudent.entities.DTO.ResponseDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.services.AuthenticationService;
import com.sloth.OnlyStudent.services.ClassroomService;
import com.sloth.OnlyStudent.shared.errors.ClassroomNameAlreadyExistsException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;
import com.sloth.OnlyStudent.shared.errors.EmailAlreadyExistsException;
import com.sloth.OnlyStudent.shared.errors.GlobalErrorHandler;

class ApiContractCharacterizationTest {

    private MockMvc mockMvc;
    private User principal;

    private final AuthenticationService authenticationService = org.mockito.Mockito.mock(AuthenticationService.class);
    private final ClassroomService classroomService = org.mockito.Mockito.mock(ClassroomService.class);
    private final ClassroomRepository classroomRepository = org.mockito.Mockito.mock(ClassroomRepository.class);
    private final MaterialRepository materialRepository = org.mockito.Mockito.mock(MaterialRepository.class);
    private final CreateClassroomUseCase createClassroomUseCase = org.mockito.Mockito.mock(CreateClassroomUseCase.class);
    private final JoinClassroomUseCase joinClassroomUseCase = org.mockito.Mockito.mock(JoinClassroomUseCase.class);
    private final ListStudentClassroomsUseCase listStudentClassroomsUseCase =
            org.mockito.Mockito.mock(ListStudentClassroomsUseCase.class);
    private final ListEducatorClassroomsUseCase listEducatorClassroomsUseCase =
            org.mockito.Mockito.mock(ListEducatorClassroomsUseCase.class);
    private final GetClassroomDetailsUseCase getClassroomDetailsUseCase =
            org.mockito.Mockito.mock(GetClassroomDetailsUseCase.class);
    private final SearchClassroomsUseCase searchClassroomsUseCase =
            org.mockito.Mockito.mock(SearchClassroomsUseCase.class);
    private final ListMaterialsUseCase listMaterialsUseCase =
            org.mockito.Mockito.mock(ListMaterialsUseCase.class);

    @BeforeEach
    void setUp() {
        principal = user(7L, UserRole.STUDENT);
        AuthenticationController authenticationController = new AuthenticationController(authenticationService);
        ClassroomController classroomController = new ClassroomController(classroomRepository, materialRepository,
                classroomService, createClassroomUseCase, joinClassroomUseCase, listStudentClassroomsUseCase,
                listEducatorClassroomsUseCase, getClassroomDetailsUseCase, searchClassroomsUseCase,
                listMaterialsUseCase);
        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType() == User.class;
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return principal;
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController, classroomController)
                .setCustomArgumentResolvers(principalResolver, new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalErrorHandler())
                .build();
    }

    @Test
    void authRegisterReturnsCreatedWithoutToken() throws Exception {
        when(authenticationService.createUser(any())).thenReturn(ResponseEntity.status(201).build());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody()))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void authLoginKeepsDirectJsonAndLowercaseRole() throws Exception {
        doReturn(ResponseEntity.ok(new ResponseDTO("Ana", "student@example.com", "student", "123", null,
                "jwt-login-token", 7L))).when(authenticationService)
                .authenticateUser(any(AuthenticationDTO.class));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@example.com\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andExpect(jsonPath("$.role").value("student"))
                .andExpect(jsonPath("$.telephone").value("123"))
                .andExpect(jsonPath("$.token").value("jwt-login-token"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void authLoginErrorRemainsPlainText() throws Exception {
        doReturn(ResponseEntity.badRequest().body("Senha incorreta."))
                .when(authenticationService).authenticateUser(any(AuthenticationDTO.class));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@example.com\",\"password\":\"wrong12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Senha incorreta."));
    }

    @Test
    void authRegisterConflictRemainsPlainText() throws Exception {
        doThrow(new EmailAlreadyExistsException()).when(authenticationService).createUser(any());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody()))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email já cadastrado."));
    }

    @Test
    void classroomRegisterKeepsSuccessTextResponse() throws Exception {
        principal = user(3L, UserRole.EDUCATOR);
        when(createClassroomUseCase.execute(any(ClassroomDTO.class), eq(3L), eq(UserRole.EDUCATOR)))
                .thenReturn(new Classroom());
        mockMvc.perform(post("/turma/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Algebra\",\"description\":\"Aula\",\"idEducator\":999,\"price\":0}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Turma criada com sucesso!"));
    }

    @Test
    void classroomRegisterConflictRemainsPlainText() throws Exception {
        principal = user(3L, UserRole.EDUCATOR);
        when(createClassroomUseCase.execute(any(ClassroomDTO.class), eq(3L), eq(UserRole.EDUCATOR)))
                .thenThrow(new ClassroomNameAlreadyExistsException());
        mockMvc.perform(post("/turma/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Algebra\",\"description\":\"Aula\",\"idEducator\":3,\"price\":0}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Já existe uma turma com esse nome."));
    }

    @Test
    void studentTurmasKeepsPageShapeAndDefaultSize() throws Exception {
        principal = user(7L, UserRole.STUDENT);
        when(listStudentClassroomsUseCase.execute(eq(7L), any()))
                .thenReturn(pageOfClassrooms());
        mockMvc.perform(get("/turma/studentTurmas").param("codigo", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(12))
                .andExpect(jsonPath("$.content[0].name").value("Algebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Edu"))
                .andExpect(jsonPath("$.content[0].totalAlunos").value(4))
                .andExpect(jsonPath("$.content[0].totalMateriais").value(2));
        verify(listStudentClassroomsUseCase).execute(eq(7L), eq(PageRequest.of(0, 50)));
    }

    @Test
    void educatorTurmasKeepsPageShapeAndDefaultSize() throws Exception {
        principal = user(3L, UserRole.EDUCATOR);
        when(listEducatorClassroomsUseCase.execute(eq(3L), any()))
                .thenReturn(pageOfClassrooms());
        mockMvc.perform(get("/turma/educatorTurmas").param("codigo", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(12))
                .andExpect(jsonPath("$.content[0].name").value("Algebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Edu"))
                .andExpect(jsonPath("$.content[0].totalAlunos").value(4))
                .andExpect(jsonPath("$.content[0].totalMateriais").value(2));
        verify(listEducatorClassroomsUseCase).execute(eq(3L), eq(PageRequest.of(0, 50)));
    }

    @Test
    void allClassroomKeepsPageShapeAndExistingSummaryFields() throws Exception {
        when(searchClassroomsUseCase.execute(eq(""), any())).thenReturn(pageOfClassrooms());
        mockMvc.perform(get("/turma/allClassroom").param("search", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value(12))
                .andExpect(jsonPath("$.content[0].name").value("Algebra"))
                .andExpect(jsonPath("$.content[0].educator").value("Edu"))
                .andExpect(jsonPath("$.content[0].status").value("ATIVA"))
                .andExpect(jsonPath("$.content[0].custo").value("GRATUITA"))
                .andExpect(jsonPath("$.content[0].price").value(0))
                .andExpect(jsonPath("$.content[0].totalAlunos").value(4))
                .andExpect(jsonPath("$.content[0].totalMateriais").value(2));
        verify(searchClassroomsUseCase).execute(eq(""), eq(PageRequest.of(0, 50)));
    }

    @Test
    void classroomDetailsKeepsDirectJsonShapeIncludingCodigo() throws Exception {
        ClassroomDetailsDTO details = new ClassroomDetailsDTO(12L, "Algebra", "Aula", "Edu", Set.of(), List.of("Ana"));
        when(getClassroomDetailsUseCase.execute(eq(12L), any())).thenReturn(details);
        mockMvc.perform(get("/turma/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(12))
                .andExpect(jsonPath("$.name").value("Algebra"))
                .andExpect(jsonPath("$.description").value("Aula"))
                .andExpect(jsonPath("$.educator").value("Edu"))
                .andExpect(jsonPath("$.nomeAlunos[0]").value("Ana"))
                .andExpect(jsonPath("$.materiais").isArray());
    }

    @Test
    void classroomJoinKeepsSuccessTextAndUsesPrincipalIdentity() throws Exception {
        principal = user(7L, UserRole.STUDENT);
        mockMvc.perform(post("/turma/12/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idAluno\":\"999\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Entrada confirmada com sucesso."));
        verify(joinClassroomUseCase).execute(12L, 7L, UserRole.STUDENT);
    }

    @Test
    void joiningMissingClassroomUsesApprovedNotFoundText() throws Exception {
        doThrow(new ClassroomNotFoundException()).when(joinClassroomUseCase)
                .execute(12L, 7L, UserRole.STUDENT);
        mockMvc.perform(post("/turma/12/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idAluno\":\"7\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Turma não encontrada."));
    }

    private PageImpl<ClassroomsDTO> pageOfClassrooms() {
        ClassroomsDTO classroom = new ClassroomsDTO(12L, "Algebra", "Edu", Status.ATIVA,
                Custo.GRATUITA, 0, 4, 2);
        return new PageImpl<>(List.of(classroom), PageRequest.of(0, 50), 1);
    }

    private String registerBody() {
        return """
                {"email":"student@example.com","telephone":"123","name":"Ana",
                 "password":"secret1","role":"STUDENT","especialidade":null}
                """;
    }

    private User user(Long id, UserRole role) {
        return new User(id, "User", "123", "user@example.com", "hash", role);
    }
}
