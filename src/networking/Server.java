package networking;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import chat.Chat;
import chat.ChatList;
import chat.ChatType;
import chat.TextMessage;
import user.User;
import user.UserLoginModule;

public class Server {
	private List<User> users = new ArrayList<>();
	private int numUsers;
	private ChatList chats;
	private List<User> onlineUsers = new ArrayList<>();
	private int numOnlineUsers;
	private static List<ClientHandler> currentClients = new ArrayList<>();
	private int numCurrentClients;
	private HashMap<String, ClientHandler> mapUsernameToClient; //string is username
	private HashMap<String, User> usernameToUser = new HashMap();
	private UserLoginModule userLoginModule = new UserLoginModule(usernameToUser); 
	
    public static void main(String[] args) throws IOException, ClassNotFoundException {
    	Server server = new Server();
    	server.startServer();
    }
    
    public void startServer() {
    	ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(7777);
            System.out.println("Server is now awaiting a new connection");

            while (true) {
                Socket socket = serverSocket.accept(); //blocks until a client connects
                ClientHandler client = new ClientHandler(socket, this);
                currentClients.add(client);
                (new Thread(client)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }

    }
    
    public User authenticateUser(User userToAuthenticate, ClientHandler handler) {
    	User user = userLoginModule.authenticateUser(userToAuthenticate);
    	if(user != null)
    		mapUsernameToClient.put(user.getUsername(), handler);
    	return user;
    }
    
    public User handleCreateNewUser(User user, ClientHandler clientHandler) throws IOException {
    	Message authenticationResponse = null;
    	User newUser = userLoginModule.createUser(user);
    	if(newUser != null) {
    		users.add(newUser);
	        authenticationResponse = new Message(MainType.CHAT_OPERATION, SubType.CREATE_USER , Status.SUCCESS, "User created successfully", newUser); //create a login success message to send to the user
    	}
    	else {
    		authenticationResponse = new Message(MainType.CHAT_OPERATION, SubType.CREATE_USER , Status.FAILED, "Failed to create new user", newUser);
    	}
    	clientHandler.sendToClient(authenticationResponse);
    	return newUser;
	}
    
    public void sendToEVERYClients(Message message) throws IOException {
    	for(ClientHandler client : currentClients) {
    		client.sendToClient(message);
    	}
    }

    public void sendToClients(Message message, String[] usernames) throws IOException {
    	for(String username : usernames) {
    		ClientHandler client = mapUsernameToClient.get(username);
    		client.sendToClient(message);
    	}
    }

    public void sendToClient(Message message, String username) throws IOException {
    	ClientHandler client = mapUsernameToClient.get(username);
    	client.sendToClient(message);
    }
    
	// MESSAGE: MainType.TEXT    
	// SubType.SEND_TEXT_MESSAGE 
    public void handleSendText(String text, String username, int chatId) throws IOException {
		User user = usernameToUser.get(username);
		TextMessage txtMsg = new TextMessage(text, username, user.getId());
		try {
			chats.addChatMessage(chatId, txtMsg);
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Invalid Chat Id of " + chatId + " detected from user: " + username);
			
			Message failedText = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE, Status.FAILED, txtMsg, chatId);
			sendToClient(failedText, username);
			return;
		}
		String[] usernames = chats.getChatMembers(chatId);
		
		for(String name : usernames) {
			User otherUser = usernameToUser.get(name);
			otherUser.updateChatOrder(chatId);
    		}
    	
        Message msgToSend = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE ,Status.SUCCESS, txtMsg, chatId);
        sendToClients(msgToSend, usernames);
    }
   
	// MESSAGE: MainType.CHAT_OPERATIONs    
	// SubType.CREATE_GC
	public void handleCreateChat(Message message, ClientHandler clientHandler) throws IOException {
		String usersToBeAddedToChat = message.getUser().getUsername() + ", "+ message.getText();
		String[] memberUsernames = usersToBeAddedToChat.split(","); //the usernames will be passed as a single string so we split
		List<String> validUsers = new ArrayList<>();
		
		
		for(int i = 0; i < memberUsernames.length; i++) {
			String userToValidate = memberUsernames[i].trim();
			if(usernameToUser.containsKey(userToValidate)) {
				validUsers.add(userToValidate);
			}
		}
		
		String[] chatUsers = validUsers.toArray(new String[0]);
		Message messageToSend;
		if(chatUsers.length >= 2) {
			Chat newChat = null;
			if(chatUsers.length == 2)
				newChat = new Chat(message.getUser().getUsername(), chatUsers, ChatType.PRIVATE);
			else
				newChat = new Chat(message.getUser().getUsername(), chatUsers, ChatType.GROUP);
			int chatId = newChat.getChatId();
			chats.addChat(newChat);
		
			for(String username : chatUsers) {
				User user = usernameToUser.get(username);
				user.addChat(newChat);
			}
			newChat = chats.getCopyOfChat(chatId);
			messageToSend = new Message(MainType.CHAT_OPERATION, SubType.ACTUAL_CHAT, Status.SUCCESS, newChat);
			sendToClients(messageToSend, chatUsers);
			return;
		}
		messageToSend = new Message(MainType.CHAT_OPERATION, SubType.ACTUAL_CHAT, Status.FAILED);
		//need to send response to client
		sendToClient(messageToSend, message.getUser().getUsername());
	}
	
	// SubType.ADD_USER_TO_GC
	public void handleAddUserToChat(Message message, ClientHandler clientHandler) throws IOException {
		int chatId = message.getChatId();
		String chatOwner = message.getUser().getUsername();
		String userToAdd= message.getText(); //we need to make sure we store the username of who is being added in the text field
		Message messageToSend;
		
		try {
			chats.addChatMember(chatId, userToAdd, chatOwner);
			messageToSend = new Message(MainType.CHAT_OPERATION, SubType.ADD_USER_TO_GC, Status.SUCCESS, "", chatOwner, chatId);
		} catch(Exception e) {
			messageToSend = new Message(MainType.CHAT_OPERATION, SubType.ADD_USER_TO_GC, Status.FAILED);
		}
		sendToClient(messageToSend, userToAdd);
	}
	
	// SubType.REMOVE_USER_FROM_GC
	public void handleRemoveUserFromChat(Message message, ClientHandler clientHandler) throws IOException {
		int chatId = message.getChatId();
		String chatOwner = message.getUser().getUsername();
		String userToRemove = message.getText(); //we need to make sure we store the username of who is being removed in the text field
		Message msgToSend;
		
		try {
			chats.removeChatMember(chatId, userToRemove, chatOwner);
			usernameToUser.get(userToRemove).removeChat(chatId, chatOwner);;
			msgToSend = new Message(MainType.CHAT_OPERATION, SubType.REMOVE_USER_FROM_GC, Status.SUCCESS, "", chatOwner, chatId);
		} catch(IndexOutOfBoundsException e) {
			msgToSend = new Message(MainType.CHAT_OPERATION, SubType.REMOVE_USER_FROM_GC, Status.FAILED);
		}
		sendToClient(msgToSend, userToRemove);
	}
	
	// SubType.DELETE_GC
	public void handleDeleteGC(Message message, ClientHandler clientHandler) {
		int chatToDelete = message.getChatId();
		String userAttemptingToDelete = message.getUser().getUsername();
		try {
			chats.deleteChat(chatToDelete, userAttemptingToDelete);
		} catch(IndexOutOfBoundsException e) {
			
		}
	}
    
    // MESSAGE: MainType.AUDIT_OPERATION
    // SubType.ENTER_AUDIT_MODE
    public boolean handleEnterAuditMode(Message message, ClientHandler clientHandler) {
		User itUser = message.getUser();
		boolean isIT = itUser.isInformationTechnologyUser();
		if(isIT) {
			return true; //temporary
		}
		return false;  //temporary
	}
    
	// SubType.SELECT_USER
    public void handleAuditSelectUser(Message message, ClientHandler clientHandler) {
		// TODO Auto-generated method stub
		
	}
    
    // SubType.VIEW_CHATS
    public void handleAuditViewChats(Message message, ClientHandler clientHandler) {
		// TODO Auto-generated method stub
		
	}
    
    // SubType.EXPORT_CHAT_LOG
    public void handleAuditExportChat(Message message, ClientHandler clientHandler) {
		// TODO Auto-generated method stub
		
	}
    
    
    //can someone clarify how to handle the message types below
    //i did not add any client side operations to handle this message type
	public void handleChatList(Message message, ClientHandler clientHandler) {
		// TODO Auto-generated method stub
		
	}

	public void handleOpenChat(Message message, ClientHandler clientHandler) {
		// TODO Auto-generated method stub
		
	}

	public void handleChatUser(Message message, ClientHandler clientHandler) {
		// TODO Auto-generated method stub
		
	}

}


