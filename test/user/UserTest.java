package user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void userConstructorNotNull() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    void defaultConstructorValues() {
        User user = new User();

        assertEquals("undefined", user.getUsername());
        assertEquals("undefined", user.getPassword());
        assertFalse(user.isOnline());
        assertFalse(user.isInformationTechnologyUser());
        assertFalse(user.isAuditMode());
        assertEquals("", user.getSessionToken());
        assertNull(user.getLastLogin());
        assertNotNull(user.getChatList());
        assertNotNull(user.getUnreadChatList());
        assertEquals(0, user.getChatCount());
        assertEquals(0, user.getUnreadChatCount());
    }

    @Test
    void constructorSetsUsernameAndPassword() {
        User user = new User("shayan", "password");

        assertEquals("shayan", user.getUsername());
        assertEquals("password", user.getPassword());
    }

    @Test
    void constructorSetsITUser() {
        User user = new User("shayan", "password", true);

        assertTrue(user.isInformationTechnologyUser());
        assertEquals("IT", user.getRole());
    }

    @Test
    void toStringReturnsUserData() {
        User user = new User("shayan", "password", false);

        assertTrue(user.toString().endsWith(",DEFAULT,shayan,password"));
    }

    @Test
    void stringConstructorSetsUserInformation() {
        User user = new User("25,IT,shayan,password");

        assertEquals(25, user.getId());
        assertTrue(user.isInformationTechnologyUser());
        assertEquals("shayan", user.getUsername());
        assertEquals("password", user.getPassword());
    }
    
    @Test
    void authenticateLoginSuccess() {
        User user = new User("shayan", "password");

        boolean result = user.authenticateLogin("shayan", "password");

        assertTrue(result);
        assertTrue(user.isOnline());
        assertFalse(user.isAuditMode());
        assertNotEquals("", user.getSessionToken());
        assertNotNull(user.getLastLogin());
    }

    @Test
    void authenticateLoginFailsWithWrongPassword() {
        User user = new User("shayan", "password");

        boolean result = user.authenticateLogin("shayan", "wrongPassword");

        assertFalse(result);
        assertFalse(user.isOnline());
    }

    @Test
    void authenticateLoginFailsWithInvalidUsernameFormat() {
        User user = new User("shayan", "password");

        boolean result = user.authenticateLogin("sha", "password");

        assertFalse(result);
        assertFalse(user.isOnline());
    }
    
    @Test
    void logoutSetsUserOffline() {
        User user = new User("shayan", "password");
        user.authenticateLogin("shayan", "password");

        user.logout();

        assertFalse(user.isOnline());
        assertFalse(user.isAuditMode());
        assertEquals("", user.getSessionToken());
    }

    @Test
    void enableAuditModeForITUser() {
        User user = new User("shayan", "password", true);
        user.authenticateLogin("shayan", "password");

        boolean result = user.enableAuditMode(user.getSessionToken());

        assertTrue(result);
        assertTrue(user.isAuditMode());
    }

    @Test
    void enableAuditModeFailsForRegularUser() {
        User user = new User("shayan", "password", false);
        user.authenticateLogin("shayan", "password");

        boolean result = user.enableAuditMode(user.getSessionToken());

        assertFalse(result);
        assertFalse(user.isAuditMode());
    }
    
    @Test
    void setITUserFalseTurnsOffAuditMode() {
        User user = new User("shayan", "password", true);
        user.authenticateLogin("shayan", "password");
        user.enableAuditMode(user.getSessionToken());

        user.setITUser(false);

        assertFalse(user.isInformationTechnologyUser());
        assertFalse(user.isAuditMode());
    }

    @Test
    void checkFormatAcceptsValidCredential() {
        assertTrue(User.checkFormat("shayan123"));
    }

    @Test
    void checkFormatRejectsShortCredential() {
        assertFalse(User.checkFormat("sha"));
    }

    @Test
    void checkFormatRejectsSpecialCharacters() {
        assertFalse(User.checkFormat("shayan123!"));
    }

    @Test
    void settersUpdateValues() {
        User user = new User();

        user.setUsername("newUser");
        user.setPassword("newPass");

        assertEquals("newUser", user.getUsername());
        assertEquals("newPass", user.getPassword());
    }
}