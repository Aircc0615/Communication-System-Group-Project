package networking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


class MessageTest {

    @Test
    void messageConstructorNotNull() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello");
        assertNotNull(message);
    }

    @Test
    void getMainType() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello");
        assertEquals(MainType.AUTHENTICATION, message.getMainType());
    }

    @Test
    void getSubType() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello");
        assertEquals(SubType.LOGIN, message.getSubType());
    }

    @Test
    void getStatus() {
        Message message = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, "hello");
        assertEquals(Status.REQUEST, message.getStatus());
    }

    @Test
    void getText() {
        String text = "test message";
        Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, text);
        assertEquals(text, message.getText());
    }

    @Test
    void getIdIncrements() {
        Message m1 = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "one");
        Message m2 = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "two");

        assertTrue(m2.getId() > m1.getId());
    }

    @Test
    void getChatId() {
    	int chatId = 5;
    	
        Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "hello", "shayan", chatId);
        assertEquals(chatId, message.getChatId());
    }

    @Test
    void getUsername() {
    	String username = "shayan";
    	
        Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.SUCCESS, "hello", username);
        assertEquals(username, message.getUsername());
    }

    @Test
    void defaultConstructorValues() {
        Message message = new Message();

        assertNotNull(message);
        assertEquals(MainType.UNDEFINED, message.getMainType());
        assertEquals(SubType.UNDEFINED, message.getSubType());
        assertEquals(Status.UNDEFINED, message.getStatus());
        assertEquals("Undefined", message.getText());
        assertNotNull(message.getDate());
        assertNotNull(message.getTimeStamp());
    }
}