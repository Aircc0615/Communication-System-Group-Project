package chat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatListTest {
	private ChatList chatList;
	private int creatorId = 1;
	private int[] groupIds = {1, 2, 3, 4, 5, 6};
	private int privateId = 2;
	
	@BeforeEach
	void initializeChatList() {
		chatList = new ChatList();
	}
	
	@Test
	void chatListConstructorNotNull() {
		assertNotNull(chatList);
	}
	
	@Test
	void chatListAddChat() {
		Chat chat = new Chat(creatorId, groupIds, ChatType.GROUP);
		chatList.addChat(chat);
		assertSame(chat, chatList.getChat(0));
	}

	@Test
	void chatListInvalidChatIndex() {
		assertThrows(IndexOutOfBoundsException.class,
				() -> chatList.getChat(0));
	}

	@Test
	void chatListNegativeChatIndex() {
		assertThrows(IndexOutOfBoundsException.class,
				() -> chatList.getChat(-1));
	}

	@Test
	void chatsSortedByNewestUpdateOnAdd() {
		Chat chat1 = new Chat(creatorId, groupIds, ChatType.GROUP);
		Chat chat2 = new Chat(creatorId, groupIds, ChatType.GROUP);
		Chat chat3 = new Chat(creatorId, groupIds, ChatType.GROUP);
		chatList.addChat(chat2);
		chatList.addChat(chat1);
		chatList.addChat(chat3);
		assertAll(
				() -> assertSame(chatList.getChat(0), chat3),
				() -> assertSame(chatList.getChat(1), chat2),
				() -> assertSame(chatList.getChat(2), chat1));
	}

	@Test
	void chatListNoDuplicates() {
		Chat chat = new Chat(creatorId, groupIds, ChatType.GROUP);
		chatList.addChat(chat);
		chatList.addChat(chat);
		assertThrows(IndexOutOfBoundsException.class,
				() -> chatList.getChat(1));
	}

	@Test
	void chatsSortedByNewestUpdateOnMessage() {
		Chat chat1 = new Chat(creatorId, groupIds, ChatType.GROUP);
		Chat chat2 = new Chat(creatorId, groupIds, ChatType.GROUP);
		Chat chat3 = new Chat(creatorId, groupIds, ChatType.GROUP);
		Chat chat4 = new Chat(creatorId, groupIds, ChatType.GROUP);
		Chat chat5 = new Chat(creatorId, groupIds, ChatType.GROUP);
		chatList.addChat(chat1);
		chatList.addChat(chat2);
		chatList.addChat(chat3);
		chatList.addChat(chat4);
		chatList.addChat(chat5);
		chatList.addChatMessage(3, new TextMessage("test", "test_user", 1));
		assertAll(
				() -> {
					for(int i = 0; i < 4; i++) {
						assertTrue(chatList.getChat(0).getNewestUpdate()
								.compareTo(chatList.getChat(i+1).getNewestUpdate()) >= 0);
					}
				}
				);
	}

	@Test
	void chatListRemoveChat() {
		Chat chat1 = new Chat(creatorId, groupIds, ChatType.GROUP);
		Chat chat2 = new Chat(creatorId, groupIds, ChatType.GROUP);
		chatList.addChat(chat1);
		chatList.addChat(chat2);

		chatList.deleteChat(0, creatorId);;

		assertSame(chat2, chatList.getChat(0));
	}
}
