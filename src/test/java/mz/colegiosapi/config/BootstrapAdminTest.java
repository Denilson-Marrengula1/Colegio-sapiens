package mz.colegiosapi.config;

import mz.colegiosapi.model.Role;
import mz.colegiosapi.model.UserAccount;
import mz.colegiosapi.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BootstrapAdminTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final BootstrapAdmin bootstrap = new BootstrapAdmin();

    @Test void createsAdminOnlyWhenEnabledAndDatabaseIsEmpty() throws Exception {
        when(encoder.encode("SenhaSegura123")).thenReturn("hash");
        bootstrap.createInitialAdmin(users, encoder, true, "admin", "SenhaSegura123", "Administrador").run();
        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(saved.capture());
        assertEquals(Role.ADMIN, saved.getValue().getRole());
        assertEquals("admin", saved.getValue().getUsername());
        assertEquals("hash", saved.getValue().getPasswordHash());
        assertTrue(saved.getValue().isActivo());
    }

    @Test void doesNotCreateAdminWhenDisabled() throws Exception {
        bootstrap.createInitialAdmin(users, encoder, false, "", "", "Administrador").run();
        verifyNoInteractions(users, encoder);
    }

    @Test void doesNotCreateAnotherAdminWhenAnyUserExists() throws Exception {
        when(users.count()).thenReturn(1L);
        bootstrap.createInitialAdmin(users, encoder, true, "outro", "SenhaSegura123", "Administrador").run();
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test void rejectsInvalidCredentialsForInitialAdmin() {
        var runner = bootstrap.createInitialAdmin(users, encoder, true, "admin", "curta", "Administrador");
        assertThrows(IllegalStateException.class, () -> runner.run());
        verify(users, never()).save(any());
    }
}
