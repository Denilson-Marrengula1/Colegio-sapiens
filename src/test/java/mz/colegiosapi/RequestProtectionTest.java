package mz.colegiosapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:requestprotection;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
class RequestProtectionTest {
    @Autowired MockMvc mvc;

    @Test void mutationRequiresCustomHeaderEvenWithValidBasicCredentials() throws Exception {
        String body="{\"codigo\":\"SEG\",\"classe\":\"10\",\"anoLetivo\":2026}";
        mvc.perform(post("/api/turmas").with(httpBasic("admin", "Admin123!"))
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/turmas").with(httpBasic("admin", "Admin123!"))
            .header("X-Requested-With", "XMLHttpRequest")
            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
    }

    @Test void crossOriginPreflightCannotAuthorizeTheCustomHeader() throws Exception {
        mvc.perform(options("/api/turmas").header("Origin", "https://outro.example")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "x-requested-with"))
            .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
