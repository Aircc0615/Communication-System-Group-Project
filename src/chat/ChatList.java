package chat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;



//might need to add a semaphore for addchat/deletechat synchronization
public class ChatList implements Serializable{
	private Chat[] chats;
	private int numChats;
	private transient Object writeMutex = new Object();

	public ChatList() {
		// default chat size
		chats = new Chat[8];
		numChats = 0;
		writeMutex = new Object();
	}

	// inserts a chat in the array based on the order of the timestamp
	public void addChat(Chat chat) {
		addThreadSafety();
		synchronized (writeMutex) {
			chat.addThreadSafety();
			int chatId = chat.getChatId();
			Chat[] tempChats = getCopyOfChats();
			int chatIndex = parseId(tempChats, chatId);
			if(chatIndex != -1)
				return;
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

	//Methods for server to call when server chat list invites users to a chat
	//Insert chat to 1 list
	public void insertChatToOneList(ChatList otherChatList, int chatId) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		otherChatList.addChat(tempChats[chatIndex]);
	}
	//Insert chat to multiple lists
	public void insertChatToMultipleLists(ChatList[] otherChatLists, int chatId) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		for(ChatList otherChatList : otherChatLists) {
			if(otherChatList == null)
				break;
			otherChatList.addChat(tempChats[chatIndex]);
		}
	}
	//

	// getters
	public TextMessage getChatMessage(int chatId, int messageIndex) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		return chats[chatIndex].getMessage(messageIndex);
	}

	public boolean chatMemberIsInChat(int chatId, String memberUsername) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		return chats[chatIndex].memberIsInChat(memberUsername);
	}

	public Instant getChatNewestUpdate(int chatId) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		return chats[chatIndex].getNewestUpdate();
	}

	// add message
	public void addChatMessage(int chatId, TextMessage message) {
		addThreadSafety();
		synchronized (writeMutex) {
			Chat[] tempChats = getCopyOfChats();
			int chatIndex = parseId(tempChats, chatId);
			if(chatIndex == -1)
				throw new IndexOutOfBoundsException();
			tempChats[chatIndex].addMessage(message);
			reorderList(tempChats, chatIndex);
			chats = tempChats;
		}
	}

	// attempt to add a member to a chat
	public void addChatMember(int chatId, String memberUsername, String fromUsername) {
		// confirm that the user is the chat owner in a group chat
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		Chat chat = tempChats[chatIndex];
		if (chat.getChatType() == ChatType.PRIVATE)
			throw new IllegalArgumentException();
		if (chat.getCreatorUsername() != fromUsername)
			throw new IllegalArgumentException();
		chat.addMember(memberUsername); // add member
	}

	// attempt to remove a member from a chat
	public void removeChatMember(int chatId, String memberUsername, String fromUsername) {
		// confirm that the user is the chat owner in a group chat
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		Chat chat = tempChats[chatIndex];
		if (chat.getChatType() == ChatType.PRIVATE)
			return;
		if (chat.getCreatorUsername() != fromUsername)
			return;
		chat.removeMember(memberUsername); // remove member
	}

	// Attempts the delete the chat with id "chatId"
	public void deleteChat(int chatId, String fromUsername) {
		addThreadSafety();
		synchronized (writeMutex) {
			Chat[] tempChats = getCopyOfChats();
			int indexInArray = parseId(tempChats, chatId);
			if (indexInArray == -1) {
				return; // Do nothing if not in array
			}
			// confirm that the user is the chat owner in a group chat
			Chat chat = tempChats[indexInArray];
			if (chat.getChatType() == ChatType.PRIVATE)
				return;
			if (chat.getCreatorUsername() != fromUsername)
				return;
			// remove the chat
			for (int i = indexInArray; i < (numChats - 1); i++) {
				tempChats[i] = tempChats[i + 1]; // shift array down 1
			}
			chats = tempChats;
			numChats--; // decrement numchats
		}
	}

	public void deleteChat(int chatId, boolean isBuffer) {
		addThreadSafety();
		synchronized (writeMutex) {
			if(!isBuffer)
				return;
			Chat[] tempChats = getCopyOfChats();
			int indexInArray = parseId(tempChats, chatId);
			if (indexInArray == -1) {
				return; // Do nothing if not in array
			}
			// confirm that the user is the chat owner in a group chat
			Chat chat = tempChats[indexInArray];
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
		int tempNumChats = numChats;
		Chat[] tempChats = chats;
		for (int i = 0; i < tempNumChats; i++) {
			if (i != 0)
				retStr += ',';
			retStr += tempChats[i].getChatId();
		}
		return retStr;
	}

	public int[] getChatIds() {
		int tempNumChats = numChats;
		int[] chatIds = new int[tempNumChats];
		Chat[] tempChats = chats;
		for (int i = 0; i < tempNumChats; i++) {
			chatIds[i] = tempChats[i].getChatId();
		}
		return chatIds;
	}

	public void updateOrder(int chatId) {
		synchronized (writeMutex) {
			Chat[] tempChats = getCopyOfChats();
			int chatIndex = parseId(tempChats, chatId);
			if(chatIndex == -1)
				return;
			reorderList(tempChats, chatIndex);
			chats = tempChats;
		}
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
		int tempNumChats = numChats;
		Chat[] tempChats = chats;
		Chat[] newChats = new Chat[tempChats.length];
		for (int i = 0; i < tempNumChats; i++) {
			newChats[i] = tempChats[i];
		}
		return newChats;
	}

	private int parseId(Chat[] chatsToParse, int chatId) {
		int tempNumChats = numChats;
		for(int i = 0; i < tempNumChats; i++) {
			if(chatsToParse[i].getChatId() == chatId) {
				return i;
			}
		}
		return -1;
	}
	
	//helper
	public int getNumChats() {
		return numChats;
	}
	public int getNumChatMembers(int chatId) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		return tempChats[chatIndex].getNumMembers();
	}
	public int getNumChatMessages(int chatId) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		return tempChats[chatIndex].getNumMessages();
	}
	public Chat getCopyOfChat(int chatId) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		return tempChats[chatIndex].getCopy();
	}

	public String[] getChatMembers(int chatId) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		return tempChats[chatIndex].getMembersInChat();
	}

	public void addThreadSafety() {
		if(writeMutex == null)
			writeMutex = new Object();
		Chat[] tempChats = chats;
		if(tempChats.length == 0) {
			tempChats = new Chat[8];
			return;
		}
		for(Chat chat : tempChats) {
			if(chat != null) {
				chat.addThreadSafety();
				System.out.println("Adding thread safety to chat: " + chat.getChatId());
			}
		}
	}

	public void exportChat(int chatId, boolean fromServer) {
		Chat[] tempChats = chats;
		int chatIndex = parseId(tempChats, chatId);
		if(chatIndex == -1)
			throw new IndexOutOfBoundsException();
		try {
			tempChats[chatIndex].exportChat(fromServer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exportChatListIds(String username) {
		String folderName;
		if(username == null)
			folderName = "Server";
		else
			folderName = "Users/" + username;
		String localPath = "";
		if(System.getProperty("user.dir").trim().contains("Communication-System-Group-Project/bin")) {
			localPath = "../";
		}
		File chatIdsDir = new File(localPath + "LocalFiles/" + folderName + "/ChatIds");
		chatIdsDir.mkdirs();
		File chatsFile = new File(localPath + "LocalFiles/" + folderName + "/ChatIds/ChatIds.txt");
		chatsFile.delete();
		try {
			chatsFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileWriter writer = null;
		try {
			writer = new FileWriter(chatsFile);
			writer.write(this.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer != null) writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void exportChatList(String username) {
		exportChatListIds(username);
		boolean fromServer = false;
		if (username == null)
			fromServer = true;
		int[] tempIds = getChatIds();
		for(int id : tempIds) {
			exportChat(id, fromServer);
		}
	}
}
