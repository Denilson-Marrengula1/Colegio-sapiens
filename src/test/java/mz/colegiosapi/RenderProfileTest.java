package mz.colegiosapi;

import mz.colegiosapi.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={
    "DB_URL=jdbc:h2:mem:renderprofile;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "DB_USER=sa", "DB_PASSWORD=test",
    "ADMIN_BOOTSTRAP_ENABLED=false", "ADMIN_USER=", "ADMIN_PASSWORD=",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@ActiveProfiles("render")
class RenderProfileTest {
    @Autowired Environment environment;
    @Autowired UserRepository users;

    @Test void renderProfileDoesNotCreateDefaultAdmin() {
        assertEquals("false", environment.getProperty("app.bootstrap.enabled"));
        assertEquals("", environment.getProperty("app.bootstrap.username"));
        assertEquals("", environment.getProperty("app.bootstrap.password"));
        assertEquals(0, users.count());
    }
}
