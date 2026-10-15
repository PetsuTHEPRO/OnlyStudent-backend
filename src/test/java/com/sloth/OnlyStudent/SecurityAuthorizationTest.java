package com.sloth.OnlyStudent;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import com.sloth.OnlyStudent.controller.AdministratorController;
import com.sloth.OnlyStudent.controller.MaterialController;
import com.sloth.OnlyStudent.application.material.CreateMaterialUseCase;
import com.sloth.OnlyStudent.application.material.DeleteMaterialUseCase;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.infra.security.SecurityConfigurations;
import com.sloth.OnlyStudent.infra.security.TokenService;
import com.sloth.OnlyStudent.repository.AdministratorRepository;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.repository.UserRepository;

@WebMvcTest(controllers = {MaterialController.class, AdministratorController.class})
@AutoConfigureMockMvc
@Import(SecurityConfigurations.class)
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MaterialRepository materialRepository;

    @MockBean
    private CreateMaterialUseCase createMaterialUseCase;

    @MockBean
    private DeleteMaterialUseCase deleteMaterialUseCase;

    @MockBean
    private ClassroomRepository classroomRepository;

    @MockBean
    private AdministratorRepository administratorRepository;

    @Test
    void materialEndpointsRequireAuthenticationAndEducatorRole() throws Exception {
        mockMvc.perform(post("/material/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Autenticação necessária."));

        mockMvc.perform(post("/material/register")
                        .with(authentication(authenticationFor(7L, UserRole.STUDENT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));
    }

    @Test
    void materialRegistrationRequiresTheAuthenticatedEducatorToOwnTheClassroom() throws Exception {
        Educator owner = new Educator(7L, "Edu", "123", "edu@example.com", "hash",
                UserRole.EDUCATOR, "Math");
        Classroom classroom = new Classroom();
        classroom.setEducator(owner);
        when(classroomRepository.findByCodigo(33L)).thenReturn(Optional.of(classroom));

        mockMvc.perform(post("/material/register")
                        .with(authentication(authenticationFor(7L, UserRole.EDUCATOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Apostila","tipo":"pdf","url":"https://example.com/a.pdf","turmaCodigo":33}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void administratorEndpointsRequireAdminRole() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Autenticação necessária."));

        mockMvc.perform(get("/admin")
                        .with(authentication(authenticationFor(7L, UserRole.EDUCATOR))))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acesso negado."));

        when(administratorRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin")
                        .with(authentication(authenticationFor(1L, UserRole.ADMIN))))
                .andExpect(status().isOk());
    }

    private UsernamePasswordAuthenticationToken authenticationFor(Long id, UserRole role) {
        User principal = new User(id, "User", "123", "user@example.com", "hash", role);
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }
}
