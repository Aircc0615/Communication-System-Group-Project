package chat;

import java.time.Instant;

//might need to add a semaphore for addchat/deletechat synchronization
public class ChatList {
	private Chat[] chats;
	private int numChats;
	private Object writeMutex;

	public ChatList() {
		// default chat size
		chats = new Chat[8];
		numChats = 0;
		writeMutex = new Object();
	}

	// inserts a chat in the array based on the order of the timestamp
	public void addChat(Chat chat) {
		synchronized (writeMutex) {
			Chat[] tempChats = getCopyOfChats();
			// makes more space if needed (2x)
			if (numChats >= tempChats.length) {
				Chat[] newChats = new Chat[tempChats.length * 2];
				for (int i = 0; i < tempChats.length; i++) {
					newChats[i] = tempChats[i];
				}
				tempChats = newChats;
			}

			tempChats[numChats] = chat;
			reorderList(tempChats, numChats);
			chats = tempChats;
			numChats++;
		}
	}

	// getters
	public TextMessage getChatMessage(int chatIndex, int messageIndex) {
		return chats[chatIndex].getMessage(messageIndex);
	}

	public int getChatMemberId(int chatIndex, int memberIndex) {
		return chats[chatIndex].getMemberId(memberIndex);
	}

	public Instant getChatNewestUpdate(int chatIndex) {
		return chats[chatIndex].getNewestUpdate();
	}

	public Chat getChat(int chatIndex) {
		return chats[chatIndex];
	}

	// add message
	public void addChatMessage(int chatIndex, TextMessage message) {
		synchronized (writeMutex) {
			Chat[] tempChats = getCopyOfChats();
			tempChats[chatIndex].addMessage(message);
			reorderList(tempChats, chatIndex);
			chats = tempChats;
		}
	}

	// attempt to add a member to a chat
	public void addChatMember(int chatIndex, int memberId, int fromId) {
		// confirm that the user is the chat owner in a group chat
		Chat chat = chats[chatIndex];
		if (chat.getChatType() == ChatType.PRIVATE)
			return;
		if (chat.getCreatorId() != fromId)
			return;
		chat.addMember(memberId); // add member
	}

	// attempt to remove a member from a chat
	public void removeChatMember(int chatIndex, int memberId, int fromId) {
		// confirm that the user is the chat owner in a group chat
		Chat chat = chats[chatIndex];
		if (chat.getChatType() == ChatType.PRIVATE)
			return;
		if (chat.getCreatorId() != fromId)
			return;
		chat.removeMember(memberId); // remove member
	}

	// Attempts the delete the chat with id "chatId"
	public void deleteChat(int chatId, int fromId) {
		synchronized (writeMutex) {
			Chat[] tempChats = getCopyOfChats();
			int indexInArray = 0; // attempts to find the chat to delete
			while (indexInArray < numChats) {
				if (tempChats[indexInArray].getChatId() == chatId) {
					break; // break if find
				}
				indexInArray++;
			}
			if (indexInArray >= numChats) {
				return; // Do nothing if not in array
			}
			// confirm that the user is the chat owner in a group chat
			Chat chat = tempChats[indexInArray];
			if (chat.getChatType() == ChatType.PRIVATE)
				return;
			if (chat.getCreatorId() != fromId)
				return;
			// remove the chat
			for (int i = indexInArray; i < (numChats - 1); i++) {
				tempChats[i] = tempChats[i + 1]; // shift array down 1
			}
			chats = tempChats;
			numChats--; // decrement numchats
		}
	}

	// returns a string of all chat ids in the list separated by ','
	public String toString() {
		String retStr = "";
		for (int i = 0; i < numChats; i++) {
			if (i != 0)
				retStr += ',';
			retStr += chats[i].getChatId();
		}
		return retStr;
	}

	private void reorderList(Chat[] tempChats, int updatedChatIndex) {
		if (updatedChatIndex == 0)
			return;
		int i;
		for (i = 0; i < updatedChatIndex; i++) {
			if (tempChats[i].getNewestUpdate().compareTo(tempChats[updatedChatIndex].getNewestUpdate()) <= 0)
				break;
		}
		if (i == updatedChatIndex)
			return;
		Chat tempChat = tempChats[updatedChatIndex];
		for (int j = updatedChatIndex; j > i; j--) {
			tempChats[j] = tempChats[j - 1];
		}
		tempChats[i] = tempChat;
	}

	private Chat[] getCopyOfChats() {
		Chat[] newChats = new Chat[chats.length];
		for (int i = 0; i < numChats; i++) {
			newChats[i] = chats[i];
		}
		return newChats;
	}
}
