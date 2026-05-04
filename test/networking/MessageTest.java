package networking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


class MessageTest {

    @Test
    void messageConstructorNotNull() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello", null);
        assertNotNull(message);
    }

    @Test
    void getMainType() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello", null);
        assertEquals(MainType.AUTHENTICATION, message.getMainType());
    }

    @Test
    void getSubType() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello", null);
        assertEquals(SubType.LOGIN, message.getSubType());
    }

    @Test
    void getStatus() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello", null);
        assertEquals(Status.REQUEST, message.getStatus());
    }

    @Test
    void getText() {
        String text = "test message";
        Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, text, null);
        assertEquals(text, message.getText());
    }

    @Test
    void getIdIncrements() {
        Message m1 = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "one", null);
        Message m2 = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "two", null);

        assertTrue(m2.getId() > m1.getId());
    }

    @Test
    void getChatId() {
        Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "hello", null);
        assertEquals("", message.getChatId());
    }

    @Test
    void getUsername() {
        Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "hello", null);
        assertEquals("", message.getUsername());
    }

    @Test
    void defaultConstructorValues() {
        Message message = new Message();

        assertNotNull(message);
        assertEquals(MainType.UNDEFINED, message.getMainType());
        assertEquals(SubType.UNDEFINED, message.getSubType());
        assertEquals(Status.UNDEFINED, message.getStatus());
        assertEquals("Undefined", message.getText());
    }
}