package networking;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import GUI.GUI;
import chat.Chat;
import chat.TextMessage;
import user.User;

public class Client {
	static InputStream serverInputStream = null;
	static ObjectInputStream objectInputStream  = null;
	static OutputStream outputStream = null;
	static ObjectOutputStream objectOutputStream = null;
	static Socket clientSideSocket = null;
	static List<Message> messageHistory = new ArrayList<>(); //client side message history
	static Scanner sin = new Scanner(System.in);
	private static User user;
	private static User selectedAuditUser;
	private static GUI gui;
	private static Thread serverListener;
	
	
		public void assignGUI(GUI gui) {
			this.gui = gui;
		}
	
    /*public static void main(String[] args) throws IOException, ClassNotFoundException {    	
    	clientSideSocket = connectToServer();
    	
        System.out.println("Please enter your username!");
		String username = sin.nextLine();
		System.out.println("Please enter your password!");
        String password = sin.nextLine();
        user = new User(username, password);
        
        User authenticatedUser = login(user); //if the user we passed is authenticated it returns the same value otherwise it returns null

        if (authenticatedUser != null) {
        	user = authenticatedUser;
					}     
        		}
    		});
             
            serverListener.start();
            String text;
            while(!clientSideSocket.isClosed()) {
            	text = sin.nextLine();
            	sendMessage(text, 0); // currently chat id of 0 (!! Must change when integrating with GUI !!)
            }
        }
    }*/
	
    // Client Side Server Operations
	// Allows Client to connect to server and returns the socket 
	public static Socket connectToServer() throws UnknownHostException, IOException {
        int port = 7777;
        String host = "localhost"; //need to update to actual host

        clientSideSocket = new Socket(host, port); //create a client side socket that connects to server with the host and port specified
        System.out.println("Connected to: " + clientSideSocket.getInetAddress().getHostAddress());

        outputStream = clientSideSocket.getOutputStream(); //output that were sending to server
        objectOutputStream = new ObjectOutputStream(outputStream); //deconstructing the object were sending, this serializes the object

        serverInputStream = clientSideSocket.getInputStream(); //whatever is coming in from the server
        objectInputStream = new ObjectInputStream(serverInputStream); // we need to reconstruct the message object
		return clientSideSocket;
	}

	public void createServerListener() {
    serverListener = new Thread(new Runnable() {
       public void run() {
        	try {
						listenForServerMessages();
					} catch (Exception e) {
						e.printStackTrace();
					}
       }
    });
    serverListener.start();
	}
	
	// Allows client to listen for incoming messages
	public void listenForServerMessages() throws ClassNotFoundException, IOException {
				System.out.println("Listening to server now");
        while(!clientSideSocket.isClosed()) {
        	Message msg = (Message) objectInputStream.readObject();
        	if(msg.mainType == MainType.DISPLAY) {
        		if(msg.subType == SubType.ACTUAL_CHAT) {
        			user.addChat(msg.getChat());
        			updateChatList();
        		}
        	} else if(msg.mainType == MainType.AUTHENTICATION) {
	          if(msg.subType == SubType.LOGOUT) {
	             System.out.println("Logging out!"); //after user logs out we can close the client side socket
	             try {
	            	 clientSideSocket.close(); //once the server actually sends the logout message the socket can close
	             } catch (IOException e) {
						// TODO Auto-generated catch block
	            	 e.printStackTrace();
	             }
	          }
	        } else if (msg.mainType == MainType.TEXT) {
	          if(msg.subType == SubType.SEND_TEXT_MESSAGE) {
	            	if(msg.status == Status.FAILED) {
	            		System.err.println("Failed message sent sent to " + msg.getChatId());
	            	} else if (msg.status == Status.SUCCESS){
	            		user.addMessageToChat(msg.getChatId(), msg.getTextMessage());
	            		updateChatList();
	            		//Put logic here to update gui view of chat/chatlist
	            		//gui.updatechatlist(msg.getChatId()) <- will update the chat as well
	            	}
	            }
	        }else { 
	            	if(msg.getUser() != null)
	            		System.out.println("\n" + msg.getUser().getUsername() + ": " + msg.getText() + '\n'); //display message along with who its from
	            	else {
	            		System.out.println("\nServer: " + msg.getText() + '\n');
	            	}
	            }
        }
	}

	//helper functions
	public static void sendToServer(Message message) throws IOException{
		objectOutputStream.writeObject(message);
	}
	
	public static void updateMessageHistory(Message message) {
		messageHistory.add(message);
	}
	
    // MESSAGE: MainType.AUTHENTICATION
    // SubType.LOGIN
	public User login(User user) throws IOException, ClassNotFoundException {
        System.out.println(user.getUsername() + " attempting to log in...");
        
        Message loginRequestMessage = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, user.getUsername() + "requesting login", user); //login message created
        updateMessageHistory(loginRequestMessage); //add the login message to the message history
        
        objectOutputStream.writeObject(loginRequestMessage); //sending the login message to server
        
        
        Message incomingLoginResponse = (Message) objectInputStream.readObject(); //deSerialized the message
        updateMessageHistory(incomingLoginResponse);
        if(incomingLoginResponse.status == Status.SUCCESS && incomingLoginResponse.subType == SubType.LOGIN_RESPONSE) {
            System.out.println(incomingLoginResponse.getText() + "\n");
            //System.out.println("Enter text to send!\n");
            User actualUser = incomingLoginResponse.getUser();
            actualUser.addChatThreadSafety();
            this.user = actualUser;
            return actualUser;
        }
        else {
        	System.out.println("Invalid Login. Please try again.");
        	return null;
        }
	}
	
    // SubType.LOGOUT
	public static void logout() throws IOException, ClassNotFoundException {
		Message logOutRequest = new Message(MainType.AUTHENTICATION, SubType.LOGOUT , Status.REQUEST, user.getUsername() + "Requesting logout...\n", user);
		updateMessageHistory(logOutRequest); //store operation in history
		sendToServer(logOutRequest);
	}
	
	public static void createNewAccount(User user) throws IOException {
		Message createAccountRequest = new Message(MainType.AUTHENTICATION, SubType.LOGOUT , Status.REQUEST, user.getUsername() + "attempting to create account...\n", user);
		updateMessageHistory(createAccountRequest); //store operation in history
		sendToServer(createAccountRequest);
	}
	
	// MESSAGE: MainType.TEXT
	// SubType.SEND_TEXT_MESSAGE
	public static void sendMessage(String text, int chatId) throws IOException {
	    Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE , Status.REQUEST, text, user.getUsername(), chatId);
	    updateMessageHistory(message); //the message the user input should be sent
	    sendToServer(message); //where the object gets serialized and sent     
	}

	
	
	// MESSAGE: MainType.DISPLAY
	// SubType.ACTUAL_CHAT
	public static void requestActualChat() throws IOException, ClassNotFoundException {
		Message actualChatRequest = new Message(MainType.DISPLAY, SubType.ACTUAL_CHAT , Status.REQUEST, null, user);
		updateMessageHistory(actualChatRequest); //store operation in history
		sendToServer(actualChatRequest);

	}
	
	// SubType.USER_STATE
	public static void getUserState() throws IOException, ClassNotFoundException {
		Message userStateRequest = new Message(MainType.DISPLAY, SubType.USER_STATE , Status.REQUEST, null, user);
		updateMessageHistory(userStateRequest); //store operation in history
		sendToServer(userStateRequest);
	}
	
	
	// MESSAGE: MainType.CHAT_OPERATIONs
	// CREATE_GC 	||       this will work for making either a DM or GC
	public void createChat(String usernames) throws IOException {
		Message createGC= new Message(MainType.CHAT_OPERATION, SubType.CREATE_GC , Status.REQUEST, usernames, user.getUsername());
		updateMessageHistory(createGC); //store operation in history
		sendToServer(createGC);
	}
	
	// SubType.ADD_USER_TO_GC
	public void addUserToChat(String username) throws IOException {
		Message addUserToGC = new Message(MainType.CHAT_OPERATION, SubType.ADD_USER_TO_GC , Status.REQUEST, username, user);
		updateMessageHistory(addUserToGC); //store operation in history
		sendToServer(addUserToGC);
	}
	
	// SubType.REMOVE_USER_FROM_GC
	public void removeUserFromChat(String username) throws IOException {
		Message removeUserFromGC = new Message(MainType.CHAT_OPERATION, SubType.REMOVE_USER_FROM_GC , Status.REQUEST, username, user);
		updateMessageHistory(removeUserFromGC); //store operation in history
		sendToServer(removeUserFromGC);
	}
	
	// SubType.DELETE_GC
	public void DeleteChat(String chatID) throws IOException {
		//need a chatID to perform, most likely the chat were hovering over/clicking on
		Message chatToDelete = new Message(MainType.CHAT_OPERATION, SubType.DELETE_GC , Status.REQUEST, chatID, user);
		updateMessageHistory(chatToDelete); //store operation in history
		sendToServer(chatToDelete);
	}	
		
	
	
	// MESSAGE: MainType.AUDIT_OPERATION
	// SubType.ENTER_AUDIT_MODE
	public void enterAuditMode() throws IOException {
		Message enterAuditMode = new Message(MainType.AUDIT_OPERATION, SubType.ENTER_AUDIT_MODE , Status.REQUEST, null, user);
		updateMessageHistory(enterAuditMode); //store operation in history
		sendToServer(enterAuditMode);
	}

	public void auditResponse(boolean isIt) {
		if(!isIt)
			return;
		//Gui Logic for switching to audit view goes here
		//
		//
	}
	
	// SubType.SELECT_USER
	public void audit_SelectUser(String username) throws IOException {
		Message selectedUser = new Message(MainType.AUDIT_OPERATION, SubType.SELECT_USER , Status.REQUEST, username, user);
		updateMessageHistory(selectedUser); //store operation in history
		sendToServer(selectedUser);
	}
	
	// SubType.VIEW_CHATS
	public void audit_ViewChats() throws IOException {
		Message viewChatsRequest = new Message(MainType.AUDIT_OPERATION, SubType.VIEW_CHATS , Status.REQUEST, null, user);
		updateMessageHistory(viewChatsRequest); //store operation in history
		sendToServer(viewChatsRequest);
	}
	
	// SubType.EXPORT_CHAT_LOG
	public void audit_ExportChatLog() throws IOException {
		Message exportLogRequest = new Message(MainType.AUDIT_OPERATION, SubType.EXPORT_CHAT_LOG , Status.REQUEST, null, user);
		updateMessageHistory(exportLogRequest); //store operation in history
		sendToServer(exportLogRequest);
	}
	
	private void updateChatList() {
	     user.addChatThreadSafety();
	     gui.reloadChatList();
	}


}
