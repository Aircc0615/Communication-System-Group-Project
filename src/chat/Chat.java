package chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Scanner;

public class Chat implements Serializable {
	private TextMessage[] messages;
	private int numMessages;
	private String[] memberUsernames;
	private int numMembers;
	private ChatType chatType;
	private String creatorUsername;
	private static int count = 1;
	private int chatId;
	private Instant newestUpdate;
	private transient Object mutexObject;
	private boolean canLoad;

	public Chat(String creatorUsername, String[] memberUsernames, ChatType type) {
		// Might need to Change this for dynamic arrays
		canLoad = false;
		numMembers = memberUsernames.length;
		boolean creatorInUsernames = false;
		for(String name : memberUsernames) {
			if(name.compareTo(creatorUsername) == 0)
				creatorInUsernames = true;
		}
		if(!creatorInUsernames)
			numMembers++;
		this.memberUsernames = new String[numMembers];
		int index = 0;
		if(!creatorInUsernames)
			this.memberUsernames[index++] = creatorUsername;
		for(String name : memberUsernames)
			this.memberUsernames[index++] = name;
		
		//
		this.creatorUsername = creatorUsername;
		chatType = type;
		newestUpdate = Instant.now();
		chatId = count++;
		messages = new TextMessage[50];
		numMessages = 0;
		mutexObject = new Object();
	}

	public Chat() {
		canLoad = true;
		messages = new TextMessage[50];
		numMessages = 0;
		mutexObject = new Object();
	}

	public boolean loadFile(String pathToFile, int chatId) {
		File chatFile = new File(pathToFile);
		try {
			if(!chatFile.createNewFile()) {
			Scanner in = new Scanner(chatFile);
			String line;
			int index = 0;
			while(in.hasNextLine()) {
				line = in.nextLine();
				switch (index) {
					case 0:
						memberUsernames = line.split(",");
						numMembers = memberUsernames.length;
						break;
					case 1:
						if(line.compareTo("PRIVATE") == 0)
							chatType = ChatType.PRIVATE;
						else if(line.compareTo("GROUP") == 0)
							chatType = ChatType.GROUP;
						break;
					case 2:
						newestUpdate = Instant.parse(line);
						break;
					case 3:
						creatorUsername = line;
						break;
					default:
						String[] textMessageInfo = line.split(",");
						TextMessage messageToAdd = new TextMessage(Integer.parseInt(textMessageInfo[0]),
								textMessageInfo[1],textMessageInfo[2],Instant.parse(textMessageInfo[3]));
						addMessage(messageToAdd);
						break;
				}
				index++;
			}
			this.chatId = chatId;
			if(count <= chatId)
				count = (chatId + 1);
			in.close();
			return true;
			}
			return false;
		} catch(Exception e) {
			return false;
		} finally {
		}
	}

	private Chat(TextMessage[] messages, int numMessages, String[] memberUsernames, 
			int numMembers, ChatType chatType,
			String creatorUsername, int chatId, Instant newestUpdate) {
		canLoad = false;
		this.messages = messages;
		this.numMessages = numMessages;
		this.memberUsernames = memberUsernames;
		this.numMembers = numMembers;
		this.chatType = chatType;
		this.creatorUsername = creatorUsername;
		this.chatId = chatId;
		this.newestUpdate = newestUpdate;
		mutexObject = new Object();
	}

	// Will need to figure out at what level of abstraction to load files
	// public Chat(String file) {}

	// inserts a message at the end of the message array
	public void addMessage(TextMessage message) {
		if(message == null){
			throw new IllegalArgumentException();
		}
		if (message.getText() == null || message.getText().trim().length() == 0){
			throw new IllegalArgumentException();
		}
		synchronized (mutexObject) {
			if (messages.length == 0) {
				messages = new TextMessage[50];
			} else if (numMessages >= messages.length) { // makes space if need be (2x)
				TextMessage[] newMessages = new TextMessage[messages.length * 2];
				for (int i = 0; i < messages.length; i++) {
					newMessages[i] = messages[i];
				}
				messages = newMessages;
			}
			int messageIndex;
			for(messageIndex = 0; messageIndex < numMessages; messageIndex++) {
				if(message.getTimestamp()
						.compareTo(messages[messageIndex].getTimestamp()) <= 0)
					break;
			}
			for(int i = numMessages; i > messageIndex; i--) {
				messages[i] = messages[i-1];
			}
			// inserts the new message and updates the timestamp for the chat
			messages[messageIndex] = message;
			if(message.getTimestamp().compareTo(newestUpdate) > 0) {
				newestUpdate = message.getTimestamp();
			}
			numMessages++;
		}
	}

	public TextMessage getMessage(int messageIndex) {
		synchronized (mutexObject) {
			if (messageIndex >= numMessages || messageIndex < 0)
				throw new IndexOutOfBoundsException();
			return messages[messageIndex];
		}
	}

	// adds a new member to the chat
	public void addMember(String username) {
		synchronized (mutexObject) {
			int index = 0;
			for(String name : memberUsernames) {
				if(index >= numMembers)
					break;
				if(name.compareTo(username) == 0) {
					throw new IllegalArgumentException();
				}
				index++;
			}
			if (numMembers >= memberUsernames.length) { // makes space if need be (2x)
				String[] newMemberUsernames = new String[memberUsernames.length * 2];
				for (int i = 0; i < memberUsernames.length; i++) {
					newMemberUsernames[i] = memberUsernames[i];
				}
				memberUsernames = newMemberUsernames;
			}
			// insert the member username
			memberUsernames[numMembers++] = username;
		}
	}

	public boolean memberIsInChat(String username) {
		synchronized (mutexObject) {
			for(int i = 0; i < numMembers; i++) {
				if(username == memberUsernames[i])
					return true;
			}
			return false;
		}
	}

	public String[] getMembersInChat() {
		synchronized (mutexObject) {
			String[] membersCopy = new String[numMembers];
			for(int i = 0; i < numMembers; i++) {
				membersCopy[i] = memberUsernames[i];
			}
			return membersCopy;
		}
	}

	// removes the member from the chat
	public void removeMember(String username) {
		synchronized (mutexObject) {
			if (username == creatorUsername) {
				throw new IllegalArgumentException();
			}
			int indexInArray = 0;
			while (indexInArray < numMembers) { // attempts to find the member
				if (memberUsernames[indexInArray].compareTo(username) == 0) {
					break;
				}
				indexInArray++;
			}
			if (indexInArray == numMembers) {
				return; // return if not found
			}
			// shift array down if found
			for (int i = indexInArray; i < (numMembers - 1); i++) {
				memberUsernames[i] = memberUsernames[i + 1];
			}
			numMembers--;
		}
	}

	// getters

	public String getCreatorUsername() {
		return creatorUsername;
	}

	public int getChatId() {
		return chatId;
	}

	public ChatType getChatType() {
		return chatType;
	}

	public Instant getNewestUpdate() {
		synchronized (mutexObject) {
			return newestUpdate;
		}
	}

	// returns string in the format:
	// member1_id,member2_id,member3_id
	// chat_type
	// chat_timestamp
	// creator_id
	// message1_userid,message1_username,message1_text,message1_timestamp
	// message2_userid,message2_username,message2_text,message2_timestamp
	// message3_userid,message3_username,message3_text,message3_timestamp
	// ...
	// messageN_userid,messageN_username,messageN_text,messageN_timestamp
	public String toString() {
		synchronized (mutexObject) {
			String retStr = "";
			// member_usernames
			for (int i = 0; i < numMembers; i++) {
				if (i != 0)
					retStr += ',';
				retStr += memberUsernames[i];
			}
			retStr += '\n';
			// chat type
			if (chatType == ChatType.PRIVATE) {
				retStr += "PRIVATE";
			}
			else if (chatType == ChatType.GROUP) {
				retStr += "GROUP";
			}
			retStr += '\n';
			// chat timestamp
			retStr += newestUpdate;
			retStr += '\n';
			// chat creator id
			retStr += creatorUsername;

			// chat messages
			TextMessage message;
			for (int i = 0; i < numMessages; i++) {
				message = messages[i];
				retStr += '\n';
				retStr += (message.getUserId() + ",");
				retStr += (message.getUsername() + ',');
				retStr += (message.getText() + ',');
				retStr += message.getTimestamp();
			}
			return retStr;
		}
	}

	public int getNumMessages() {
		synchronized (mutexObject) {
			return numMessages;
		}
	}

	public int getNumMembers() {
		synchronized (mutexObject) {
			return numMembers;
		}
	}

	public Chat getCopy() {
		synchronized (mutexObject) {
			TextMessage[] copyMessages = new TextMessage[numMessages];
			for (int i = 0; i < numMessages; i++) {
				copyMessages[i] = messages[i];
			}
			String[] copyMembers = new String[numMembers];
			for (int i = 0; i < numMembers; i++) {
				copyMembers[i] = memberUsernames[i];
			}
			return new Chat(copyMessages, numMessages, copyMembers, numMembers, chatType, creatorUsername, chatId,
			    newestUpdate);
		}
	}

	public void addThreadSafety() {
		if(mutexObject == null)
			mutexObject = new Object();
	}

	public void exportChat(boolean fromServer) throws IOException {
		String folderName;
		if(fromServer)
			folderName = "Server";
		else
			folderName = "IT_Export";
		String localPath = "";
		if(System.getProperty("user.dir").trim().contains("Communication-System-Group-Project/bin")) {
			localPath += "../";
		}
		File chatDir = new File(localPath + "LocalFiles/" + folderName + "/Chats");
		chatDir.mkdirs();
		File chatFile = new File(localPath + "LocalFiles/" + folderName + "/Chats/Chat_" + chatId + ".txt");
		chatFile.delete();
		chatFile.createNewFile();
		FileWriter writer = new FileWriter(chatFile);
		writer.write(this.toString());
		writer.close();
	}
	public String getMemberUsername(int index) {
	    return memberUsernames[index];
	}
}