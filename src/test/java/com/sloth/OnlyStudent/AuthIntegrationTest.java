package com.sloth.OnlyStudent;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sloth.OnlyStudent.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registersStudentWithCreatedAndEmptyBody() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("student", "STUDENT", null)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void registersEducatorWithCreatedAndEmptyBody() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("educator", "EDUCATOR", "Matemática")))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void duplicateEmailReturnsConflictAndPlainText() throws Exception {
        String request = registerRequest("duplicate", "STUDENT", null);
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Email já cadastrado."));
    }

    @Test
    void validLoginReturnsExactResponseDtoShapeWithLowercaseRole() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("login", "STUDENT", null)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@example.com","password":"secret1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Login User"))
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.role").value("student"))
                .andExpect(jsonPath("$.telephone").value("555-0100"))
                .andExpect(jsonPath("$.especialidade").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        Set<String> fields = new java.util.HashSet<>();
        json.fieldNames().forEachRemaining(fields::add);
        org.junit.jupiter.api.Assertions.assertEquals(
                Set.of("name", "email", "role", "telephone", "especialidade", "token", "id"), fields);
    }

    @Test
    void incorrectPasswordReturnsBadRequestAndPlainText() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("wrong-password", "STUDENT", null)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"wrong-password@example.com","password":"invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Senha incorreta."));
    }

    @Test
    void unknownEmailReturnsNotFoundAndPlainText() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"missing@example.com","password":"secret1"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Email não cadastrado, acesse a página de cadastro!"));
    }

    @Test
    void adminRoleRegistrationReturnsBadRequestAndPlainText() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest("admin", "ADMIN", null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(
                        "O cadastro está disponível apenas para alunos e educadores."));
    }

    @Test
    void invalidRequestReturnsPlainTextValidationMessage() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bad-email","telephone":"","name":"","password":"123",
                                 "role":"EDUCATOR","especialidade":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is("Telefone é obrigatório."),
                        org.hamcrest.Matchers.is("Nome é obrigatório."),
                        org.hamcrest.Matchers.is("A senha deve ter no mínimo 6 caracteres."),
                        org.hamcrest.Matchers.is("Email inválido."),
                        org.hamcrest.Matchers.is("A especialidade é obrigatória para educadores."))));
    }

    private String registerRequest(String localPart, String role, String specialty) {
        String email = localPart + "@example.com";
        String specialtyJson = specialty == null ? "null" : "\"" + specialty + "\"";
        return """
                {"email":"%s","telephone":"555-0100","name":"Login User",
                 "password":"secret1","role":"%s","especialidade":%s}
                """.formatted(email, role, specialtyJson);
    }
}
