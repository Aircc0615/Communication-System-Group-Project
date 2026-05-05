package user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

class UserLoginModuleTest {

    @Test
    void authenticateUserSuccess() {
        HashMap<String, User> map = new HashMap<>();
        List<User> users = new ArrayList<>();

        User existingUser = new User("shayan", "password");
        map.put("shayan", existingUser);

        UserLoginModule module = new UserLoginModule(map, users);

        User loginAttempt = new User("shayan", "password");

        User result = module.authenticateUser(loginAttempt);

        assertNotNull(result);
        assertEquals("shayan", result.getUsername());
    }

    @Test
    void authenticateUserFailsWrongPassword() {
        HashMap<String, User> map = new HashMap<>();
        List<User> users = new ArrayList<>();

        User existingUser = new User("shayan", "password");
        map.put("shayan", existingUser);

        UserLoginModule module = new UserLoginModule(map, users);

        User loginAttempt = new User("shayan", "wrong");

        User result = module.authenticateUser(loginAttempt);

        assertNull(result);
    }

    @Test
    void authenticateUserFailsUserNotFound() {
        HashMap<String, User> map = new HashMap<>();
        List<User> users = new ArrayList<>();

        UserLoginModule module = new UserLoginModule(map, users);

        User loginAttempt = new User("unknown", "password");

        User result = module.authenticateUser(loginAttempt);

        assertNull(result);
    }

    @Test
    void createUserSuccess() {
        HashMap<String, User> map = new HashMap<>();
        List<User> users = new ArrayList<>();

        UserLoginModule module = new UserLoginModule(map, users);

        User newUser = new User("newUser", "password");

        User result = module.createUser(newUser);

        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertTrue(map.containsKey("newUser"));
    }

    @Test
    void createUserFailsIfUsernameExists() {
        HashMap<String, User> map = new HashMap<>();
        List<User> users = new ArrayList<>();

        User existingUser = new User("shayan", "password");
        map.put("shayan", existingUser);

        UserLoginModule module = new UserLoginModule(map, users);

        User newUser = new User("shayan", "password");

        User result = module.createUser(newUser);

        assertNull(result);
    }
}