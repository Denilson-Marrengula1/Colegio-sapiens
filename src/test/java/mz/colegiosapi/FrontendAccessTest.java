package mz.colegiosapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import mz.colegiosapi.model.Role;
import mz.colegiosapi.model.UserAccount;
import mz.colegiosapi.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:frontend;DB_CLOSE_DELAY=-1",
    "app.bootstrap.username=uiadmin","app.bootstrap.password=UiAdmin123!",
    "spring.h2.console.enabled=false"})
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(MockMvcRequestHeaders.class)
class FrontendAccessTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    @Test void frontendIsPublicButUserListIsProtected() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/assets/app.js")).andExpect(status().isOk());
        mvc.perform(get("/assets/styles.css")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/utilizadores")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(username="uiadmin",roles="ADMIN") @org.springframework.transaction.annotation.Transactional
    void adminAccountsCannotBeEditedButOwnPasswordCanBeChanged() throws Exception {
        UserAccount admin=users.findByUsername("uiadmin").orElseThrow();
        UserAccount other=new UserAccount();
        other.setUsername("outro-admin");other.setNome("Outro Administrador");other.setRole(Role.ADMIN);
        other.setPasswordHash(encoder.encode("OutroAdmin123!"));other.setActivo(true);
        users.saveAndFlush(other);

        for(UserAccount target:new UserAccount[]{admin,other}) {
            mvc.perform(put("/api/auth/utilizadores/{id}",target.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"%s","nome":"Alterado","password":"Alterado123!","role":"ADMIN"}
                    """.formatted(target.getUsername())))
                .andExpect(status().isForbidden());
            assertNotEquals("Alterado",users.findById(target.getId()).orElseThrow().getNome());
        }
        assertTrue(encoder.matches("UiAdmin123!",admin.getPasswordHash()));
        assertTrue(encoder.matches("OutroAdmin123!",other.getPasswordHash()));

        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"UiAdmin123!\",\"newPassword\":\"NovaSenha123!\"}"))
            .andExpect(status().isOk());
        assertTrue(encoder.matches("NovaSenha123!",users.findById(admin.getId()).orElseThrow().getPasswordHash()));
    }

    @Test @WithMockUser(username="uiadmin",roles="ADMIN")
    void userLifecycleNeverExposesHashAndPreventsSelfDeactivation() throws Exception {
        String cadastro=mvc.perform(post("/api/professores").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nome":"Professor Teste","numeroFuncionario":"UI-PROF","email":"professor@example.com","telefone":"841234567"}
                """)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long pid=json.readTree(cadastro).get("id").asLong();
        long id=users.findByProfessorId(pid).orElseThrow().getId();
        mvc.perform(put("/api/auth/utilizadores/{id}",id).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"ui-professor","nome":"Professor Teste","password":"Professor123!",
                 "role":"PROFESSOR","email":"professor@example.com","telefone":"841234567"}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(patch("/api/auth/utilizadores/{id}/estado",id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"activo\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/utilizadores")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        mvc.perform(put("/api/auth/utilizadores/{id}",id).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"ui-professor","nome":"Professor Editado","password":"",
                 "role":"PROFESSOR","email":null,"telefone":"841234568"}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Professor Editado"));
        assertTrue(encoder.matches("Professor123!",users.findById(id).orElseThrow().getPasswordHash()));
        mvc.perform(patch("/api/auth/utilizadores/{id}/estado",id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"activo\":false}")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"ui-professor\",\"password\":\"Professor123!\"}"))
            .andExpect(status().isUnauthorized());
        long adminId=users.findByUsername("uiadmin").orElseThrow().getId();
        mvc.perform(patch("/api/auth/utilizadores/{id}/estado",adminId).contentType(MediaType.APPLICATION_JSON)
            .content("{\"activo\":false}")).andExpect(status().isConflict());
        mvc.perform(put("/api/auth/utilizadores/{id}",adminId).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"uiadmin","nome":"Administrador","password":"","role":"PROFESSOR"}
                """)).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles="SECRETARIA")
    void secretariaCannotEditPrivilegedAccountsOrChangeAccountState() throws Exception {
        long adminId=users.findByUsername("uiadmin").orElseThrow().getId();
        mvc.perform(put("/api/auth/utilizadores/{id}",adminId).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"uiadmin","nome":"Alterado","password":"","role":"PROFESSOR"}
                """)).andExpect(status().isForbidden());
        mvc.perform(put("/api/auth/utilizadores/{id}",adminId).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"uiadmin","nome":"Alterado","password":"","role":"ADMIN"}
                """)).andExpect(status().isForbidden());
        assertEquals(Role.ADMIN,users.findById(adminId).orElseThrow().getRole());
        assertNotEquals("Alterado",users.findById(adminId).orElseThrow().getNome());
        mvc.perform(patch("/api/auth/utilizadores/{id}/estado",adminId).contentType(MediaType.APPLICATION_JSON)
            .content("{\"activo\":false}")).andExpect(status().isForbidden());
    }
}
