package mz.colegiosapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestConfiguration
class MockMvcRequestHeaders {
    @Bean MockMvcBuilderCustomizer defaultRequestHeaders() {
        return builder -> builder.defaultRequest(get("/").header("X-Requested-With", "XMLHttpRequest"));
    }
}
