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
	private HashMap<String, ClientHandler> mapUsernameToClient = new HashMap(); //string is username
	private HashMap<String, User> usernameToUser = new HashMap();
	private UserLoginModule userLoginModule = new UserLoginModule(usernameToUser); 
	
    public static void main(String[] args) throws IOException, ClassNotFoundException {
    	Server server = new Server();
    	server.createTestUsers();
    	server.startServer();
    }
    
    //used to test GUI
    public void createTestUsers() {
    	User user1 = new User("user1", "pw");
    	User user2 = new User("user2", "pw");
    	User user3 = new User("user3", "pw");
    	User user4 = new User("user4", "pw");
    	User user5 = new User("user5", "pw");
    	User user6 = new User("user6", "pw");
    	
    	users.add(user1);
    	users.add(user2);
    	users.add(user3);
    	users.add(user4);
    	users.add(user5);
    	users.add(user6);
    	
    	usernameToUser.put(user1.getUsername(), user1);
    	usernameToUser.put(user2.getUsername(), user2);
    	usernameToUser.put(user3.getUsername(), user3);
    	usernameToUser.put(user4.getUsername(), user4);
    	usernameToUser.put(user5.getUsername(), user5);
    	usernameToUser.put(user6.getUsername(), user6);
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
    
    public User authenticateUser(User userToAuthenticate, ClientHandler handler) throws IOException {
    	System.out.println("Authenticating User");
    	User user = userLoginModule.authenticateUser(userToAuthenticate);
    	if(user != null) {
    		mapUsernameToClient.put(user.getUsername(), handler);
    		System.out.println("Successful");
    		Message authenticationResponse = new Message(MainType.AUTHENTICATION, SubType.LOGIN_RESPONSE, Status.SUCCESS, user.getUsername(), user);
    		sendToClient(authenticationResponse, user.getUsername());
    	}
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
	public void handleCreateChat(Message message, ClientHandler clientHandler) {
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
		
		Chat newChat = null;
		if(chatUsers.length == 2) {
			newChat = new Chat(message.getUser().getUsername(), chatUsers, ChatType.PRIVATE);
		}
		else {
			newChat = new Chat(message.getUser().getUsername(), chatUsers, ChatType.GROUP);
		}
		chats.addChat(newChat);
		
		//need to send response to client
	}
	
	// SubType.ADD_USER_TO_GC
	public void handleAddUserToChat(Message message, ClientHandler clientHandler) {
		int chatId = message.getChatId();
		String chatOwner = message.getUser().getUsername();
		String userToAdd= message.getText(); //we need to make sure we store the username of who is being added in the text field
		
		try {
			chats.addChatMember(chatId, userToAdd, chatOwner);
		} catch(IndexOutOfBoundsException e) {
			
		}
	}
	
	// SubType.REMOVE_USER_FROM_GC
	public void handleRemoveUserFromChat(Message message, ClientHandler clientHandler) {
		int chatId = message.getChatId();
		String chatOwner = message.getUser().getUsername();
		String userToRemove = message.getText(); //we need to make sure we store the username of who is being removed in the text field
		
		try {
		chats.removeChatMember(chatId, userToRemove, chatOwner);
		} catch(IndexOutOfBoundsException e) {
			
		}
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


