package mz.colegiosapi.config;

import mz.colegiosapi.model.Role;
import mz.colegiosapi.model.UserAccount;
import mz.colegiosapi.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdmin {
    @Bean CommandLineRunner createInitialAdmin(UserRepository users, PasswordEncoder encoder,
            @Value("${app.bootstrap.enabled:true}") boolean enabled,
            @Value("${app.bootstrap.username}") String username,
            @Value("${app.bootstrap.password}") String password,
            @Value("${app.bootstrap.name}") String name) {
        return args -> {
            if (enabled && users.count() == 0) {
                if (username == null || username.isBlank() || username.length() > 60 || username.contains(":"))
                    throw new IllegalStateException("Configure um nome de administrador válido (até 60 caracteres, sem dois-pontos)");
                if (password == null || password.isBlank() || password.length() < 8 || password.length() > 72)
                    throw new IllegalStateException("Configure uma palavra-passe inicial com 8 a 72 caracteres");
                if (name == null || name.isBlank() || name.length() > 120)
                    throw new IllegalStateException("Configure um nome de administrador com até 120 caracteres");
                UserAccount admin = new UserAccount();
                admin.setUsername(username); admin.setNome(name); admin.setRole(Role.ADMIN);
                admin.setPasswordHash(encoder.encode(password)); admin.setActivo(true);
                users.save(admin);
            }
        };
    }
}
