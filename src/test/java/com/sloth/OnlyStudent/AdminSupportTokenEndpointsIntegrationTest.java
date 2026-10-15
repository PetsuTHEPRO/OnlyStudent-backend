package com.sloth.OnlyStudent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.sloth.OnlyStudent.application.administrator.CreateAdministratorUseCase;
import com.sloth.OnlyStudent.application.administrator.DeleteAdministratorUseCase;
import com.sloth.OnlyStudent.application.administrator.GetAdministratorUseCase;
import com.sloth.OnlyStudent.application.administrator.ListAdministratorsUseCase;
import com.sloth.OnlyStudent.application.administrator.UpdateAdministratorUseCase;
import com.sloth.OnlyStudent.application.support.SendSupportEmailUseCase;
import com.sloth.OnlyStudent.controller.AdministratorController;
import com.sloth.OnlyStudent.controller.SupportController;
import com.sloth.OnlyStudent.controller.TokenController;
import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.infra.security.SecurityConfigurations;
import com.sloth.OnlyStudent.infra.security.TokenService;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.GlobalErrorHandler;

@WebMvcTest(controllers = {AdministratorController.class, SupportController.class, TokenController.class})
@AutoConfigureMockMvc
@Import({SecurityConfigurations.class, GlobalErrorHandler.class})
class AdminSupportTokenEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ListAdministratorsUseCase listAdministratorsUseCase;

    @MockBean
    private GetAdministratorUseCase getAdministratorUseCase;

    @MockBean
    private CreateAdministratorUseCase createAdministratorUseCase;

    @MockBean
    private UpdateAdministratorUseCase updateAdministratorUseCase;

    @MockBean
    private DeleteAdministratorUseCase deleteAdministratorUseCase;

    @MockBean
    private SendSupportEmailUseCase sendSupportEmailUseCase;

    @Test
    void adminCrudKeepsPathsFieldsAndNeverSerializesPassword() throws Exception {
        AdministratorResponseDTO response = administratorResponse();
        when(listAdministratorsUseCase.execute()).thenReturn(List.of(response));
        when(getAdministratorUseCase.execute(3L)).thenReturn(Optional.of(response));
        when(createAdministratorUseCase.execute(any())).thenReturn(response);
        when(updateAdministratorUseCase.execute(org.mockito.ArgumentMatchers.eq(3L), any()))
                .thenReturn(Optional.of(response));
        when(deleteAdministratorUseCase.execute(3L)).thenReturn(true);

        mockMvc.perform(get("/admin").with(as(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].name").value("Admin"))
                .andExpect(jsonPath("$[0].telephone").value("555"))
                .andExpect(jsonPath("$[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[0].roles").value("ADMIN"))
                .andExpect(jsonPath("$[0].password").doesNotExist());

        mockMvc.perform(get("/admin/3").with(as(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/admin").with(as(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(put("/admin/3").with(as(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(delete("/admin/3").with(as(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        mockMvc.perform(get("/admin/admin-resource").with(as(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string("Resource for Admins"));
    }

    @Test
    void studentAndEducatorAreForbiddenFromAdministratorRoutes() throws Exception {
        for (UserRole role : List.of(UserRole.STUDENT, UserRole.EDUCATOR)) {
            mockMvc.perform(get("/admin").with(as(role)))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Acesso negado."));
            mockMvc.perform(get("/admin/admin-resource").with(as(role)))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string("Acesso negado."));
        }
    }

    @Test
    void supportRemainsPublicAndValidatesInputWithoutChangingSuccessText() throws Exception {
        mockMvc.perform(post("/support/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"ana@example.com","phone":"555","razao":"Dúvida","descricao":"Preciso de ajuda"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully"));
        verify(sendSupportEmailUseCase).execute(any());

        mockMvc.perform(post("/support/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"ana@example.com","phone":"555","razao":"Dúvida","descricao":"Texto"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nome é obrigatório."));
    }

    @Test
    void tokenValidateKeepsPublicSuccessAndInvalidResponses() throws Exception {
        when(tokenService.validateToken("valid-token")).thenReturn("user@example.com");
        when(tokenService.validateToken("invalid-token")).thenReturn(null);

        mockMvc.perform(get("/token/validate").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid."));
        mockMvc.perform(get("/token/validate").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token is invalid or expired."));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor as(UserRole role) {
        User principal = new User(3L, "Admin", "555", "admin@example.com", "hash", role);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    private AdministratorResponseDTO administratorResponse() {
        return new AdministratorResponseDTO(3L, "Admin", "555", "admin@example.com",
                UserRole.ADMIN, UserRole.ADMIN);
    }

    private String adminRequest() {
        return """
                {"email":"admin@example.com","telephone":"555","name":"Admin","password":"secret1","role":"ADMIN"}
                """;
    }
}
