package com.sloth.OnlyStudent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.sloth.OnlyStudent.application.profile.EducatorProfileMapper;
import com.sloth.OnlyStudent.application.profile.GetEducatorProfileUseCase;
import com.sloth.OnlyStudent.application.profile.GetStudentProfileUseCase;
import com.sloth.OnlyStudent.application.profile.StudentProfileMapper;
import com.sloth.OnlyStudent.application.profile.UpdateEducatorProfileUseCase;
import com.sloth.OnlyStudent.application.profile.UpdateStudentProfileUseCase;
import com.sloth.OnlyStudent.controller.EducatorController;
import com.sloth.OnlyStudent.controller.StudentController;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.infra.security.SecurityConfigurations;
import com.sloth.OnlyStudent.infra.security.TokenService;
import com.sloth.OnlyStudent.repository.EducatorRepository;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.GlobalErrorHandler;

@WebMvcTest(controllers = {StudentController.class, EducatorController.class})
@AutoConfigureMockMvc
@Import({SecurityConfigurations.class, GlobalErrorHandler.class, StudentProfileMapper.class,
        EducatorProfileMapper.class, GetStudentProfileUseCase.class, UpdateStudentProfileUseCase.class,
        GetEducatorProfileUseCase.class, UpdateEducatorProfileUseCase.class})
class ProfileEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private EducatorRepository educatorRepository;

    @Test
    void studentReadsOwnProfileWithContractFields() throws Exception {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student()));

        mockMvc.perform(get("/student/atualUser")
                        .with(authentication(authenticationFor(7L, UserRole.STUDENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.telefone").value("111"))
                .andExpect(jsonPath("$.login").value("ana@example.com"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
        verify(studentRepository).findById(7L);
    }

    @Test
    void studentUpdatesOwnProfile() throws Exception {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student()));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/student/updateProfile")
                        .with(authentication(authenticationFor(7L, UserRole.STUDENT)))
                        .queryParam("login", "ana@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileRequest("Ana Nova", "222", "ignored@example.com", "ignored")))
                .andExpect(status().isOk())
                .andExpect(content().string("Salvado com Sucesso!"));
        verify(studentRepository).findById(7L);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void studentCannotReadAnotherStudentProfile() throws Exception {
        mockMvc.perform(get("/student/atualUser")
                        .queryParam("id", "8")
                        .with(authentication(authenticationFor(7L, UserRole.STUDENT))))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void studentCannotUpdateAnotherStudentProfile() throws Exception {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student()));

        mockMvc.perform(put("/student/updateProfile")
                        .queryParam("login", "other@example.com")
                        .with(authentication(authenticationFor(7L, UserRole.STUDENT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileRequest("Intruso", "222", "other@example.com", null)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void educatorReadsOwnProfileWithContractFieldsAndIdByEmailReturnsPrincipalId() throws Exception {
        when(educatorRepository.findById(9L)).thenReturn(Optional.of(educator()));

        mockMvc.perform(get("/educator/atualUser")
                        .with(authentication(authenticationFor(9L, UserRole.EDUCATOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Edu"))
                .andExpect(jsonPath("$.telefone").value("333"))
                .andExpect(jsonPath("$.login").value("edu@example.com"))
                .andExpect(jsonPath("$.especialidade").value("Math"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/educator/idByEmail")
                        .queryParam("email", "someone-else@example.com")
                        .with(authentication(authenticationFor(9L, UserRole.EDUCATOR))))
                .andExpect(status().isOk())
                .andExpect(content().json("9"));
    }

    @Test
    void educatorUpdatesOwnProfile() throws Exception {
        when(educatorRepository.findById(9L)).thenReturn(Optional.of(educator()));
        when(educatorRepository.save(any(Educator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/educator/updateProfile")
                        .queryParam("login", "edu@example.com")
                        .with(authentication(authenticationFor(9L, UserRole.EDUCATOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileRequest("Edu Novo", "444", "ignored@example.com", "Science")))
                .andExpect(status().isOk())
                .andExpect(content().string("Salvado com Sucesso!"));
        verify(educatorRepository).findById(9L);
        verify(educatorRepository).save(any(Educator.class));
    }

    @Test
    void educatorCannotReadAnotherEducatorProfile() throws Exception {
        mockMvc.perform(get("/educator/atualUser")
                        .queryParam("id", "10")
                        .with(authentication(authenticationFor(9L, UserRole.EDUCATOR))))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void educatorCannotUpdateAnotherEducatorProfile() throws Exception {
        when(educatorRepository.findById(9L)).thenReturn(Optional.of(educator()));

        mockMvc.perform(put("/educator/updateProfile")
                        .queryParam("login", "other@example.com")
                        .with(authentication(authenticationFor(9L, UserRole.EDUCATOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileRequest("Intruso", "444", "other@example.com", "Science")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void wrongRolesCannotAccessStudentOrEducatorProfileRoutes() throws Exception {
        var studentAuth = authentication(authenticationFor(7L, UserRole.STUDENT));
        var educatorAuth = authentication(authenticationFor(9L, UserRole.EDUCATOR));

        mockMvc.perform(get("/student/atualUser").with(educatorAuth))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
        mockMvc.perform(put("/student/updateProfile")
                        .with(educatorAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileRequest("Name", "222", "name@example.com", null)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
        mockMvc.perform(get("/educator/atualUser").with(studentAuth))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
        mockMvc.perform(put("/educator/updateProfile")
                        .with(studentAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileRequest("Name", "222", "name@example.com", "Math")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
        mockMvc.perform(get("/educator/idByEmail").with(studentAuth))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    private UsernamePasswordAuthenticationToken authenticationFor(Long id, UserRole role) {
        User principal = new User(id, "User", "111", "user@example.com", "hash", role);
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    private Student student() {
        return new Student(7L, "Ana", "111", "ana@example.com", "hash", UserRole.STUDENT);
    }

    private Educator educator() {
        return new Educator(9L, "Edu", "333", "edu@example.com", "hash", UserRole.EDUCATOR, "Math");
    }

    private String profileRequest(String name, String telephone, String login, String specialty) {
        return """
                {"name":"%s","telefone":"%s","login":"%s","especialidade":%s}
                """.formatted(name, telephone, login,
                specialty == null ? "null" : "\"" + specialty + "\"");
    }
}
