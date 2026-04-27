package com.bloodhound2.service;

import com.bloodhound2.config.DatabaseConfig;
import com.bloodhound2.dao.UserDao;
import com.bloodhound2.model.User;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void register_successfulRegistration_returnsUserWithoutPasswordHash() throws Exception {
        FakeUserDao dao = new FakeUserDao();
        dao.nextInsertId = 42L;
        AuthService service = new AuthService(dao);

        User created = service.register("  alex  ", " alex@example.com ", "password123");

        assertEquals(42L, created.getUserId());
        assertEquals("alex", created.getUsername());
        assertEquals("alex@example.com", created.getEmail());
        assertNull(created.getPasswordHash());
        assertNotNull(dao.lastInsertedUser);
        assertTrue(BCrypt.checkpw("password123", dao.lastInsertedUser.getPasswordHash()));
    }

    @Test
    void register_duplicateUsernameOrEmail_rejected() {
        FakeUserDao dao = new FakeUserDao();
        dao.insertException = new SQLException("Duplicate key", "23000", 1062);
        AuthService service = new AuthService(dao);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.register("alex", "alex@example.com", "password123"));

        assertTrue(ex.getMessage().contains("already registered"));
    }

    @Test
    void login_successfulLogin_returnsUserWithoutPasswordHash() throws Exception {
        FakeUserDao dao = new FakeUserDao();
        dao.userByUsername =
                Optional.of(new User(7L, "alex", "alex@example.com", BCrypt.hashpw("password123", BCrypt.gensalt())));
        AuthService service = new AuthService(dao);

        Optional<User> loggedIn = service.login("alex", "password123");

        assertTrue(loggedIn.isPresent());
        assertEquals(7L, loggedIn.get().getUserId());
        assertEquals("alex", loggedIn.get().getUsername());
        assertNull(loggedIn.get().getPasswordHash());
    }

    @Test
    void login_wrongPassword_returnsEmpty() throws Exception {
        FakeUserDao dao = new FakeUserDao();
        dao.userByUsername =
                Optional.of(new User(7L, "alex", "alex@example.com", BCrypt.hashpw("correct-password", BCrypt.gensalt())));
        AuthService service = new AuthService(dao);

        Optional<User> loggedIn = service.login("alex", "wrong-password");

        assertTrue(loggedIn.isEmpty());
    }

    private static final class FakeUserDao extends UserDao {
        private long nextInsertId = 1L;
        private SQLException insertException;
        private Optional<User> userByUsername = Optional.empty();
        private User lastInsertedUser;

        private FakeUserDao() {
            super(new DatabaseConfig());
        }

        @Override
        public long insert(User user) throws SQLException {
            if (insertException != null) {
                throw insertException;
            }
            this.lastInsertedUser = user;
            return nextInsertId;
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return userByUsername;
        }
    }
}
