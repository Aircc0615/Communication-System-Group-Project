package networking;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import GUI.GUI;
import chat.Chat;
import chat.TextMessage;
import user.User;

public class Client {
	InputStream serverInputStream = null;
	ObjectInputStream objectInputStream  = null;
	OutputStream outputStream = null;
	ObjectOutputStream objectOutputStream = null;
	Socket clientSideSocket = null;
	List<Message> messageHistory = new ArrayList<>(); //client side message history
	Scanner sin = new Scanner(System.in);
	private User user;
	private User selectedAuditUser;
	private GUI gui;
	private Thread serverListener;
	
	
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
	public Socket connectToServer() throws UnknownHostException, IOException {
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
        	if(msg.mainType == MainType.SERVER && msg.subType == SubType.EXIT) {
        		gui.forceExit();
        	} else if(msg.mainType == MainType.DISPLAY) {
        		if(msg.subType == SubType.ACTUAL_CHAT) {
        			user.addChat(msg.getChat());
        			updateChatList(user);
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
	            		updateChatList(user);
	            	}
	            }
	        } 
	        else if (msg.mainType == MainType.AUDIT_OPERATION) {
	        	User tempUser;
	        	switch (msg.getSubType()) {
	        		case SubType.SELECT_USER:
	        			if(msg.getStatus() != Status.SUCCESS)
	        				break;
	        			tempUser = msg.getUser();
	        			if (tempUser == null) {
	        		        break;
	        		    }
	        		    tempUser.addChatThreadSafety();
	        		    tempUser.chatListSelfCopyChats();
	        				selectedAuditUser = tempUser;

	        		    SwingUtilities.invokeLater(() -> {
	        		        gui.setNewAuditUser(selectedAuditUser);
	        		        gui.reloadChatList(selectedAuditUser);
	        		    });
	        			break;
	        		case SubType.REMOVE_USER_FROM_GC: 
	        			tempUser = selectedAuditUser;
	        			if(tempUser.getUsername().compareTo(msg.getUsername()) != 0) {
	        				break;
	        			}
	        			tempUser.addChat(msg.getChat());
	        			updateChatList(tempUser);
	        			break;
	        		case SubType.ADD_USER_TO_GC: 
	        			tempUser = selectedAuditUser;
	        			if(tempUser.getUsername().compareTo(msg.getUsername()) != 0) {
	        				break;
	        			}
	        			tempUser.removeChat(msg.getChatId(), msg.getUsername());
	        			updateChatList(tempUser);
	        			break;
	        		case SubType.ACTUAL_CHAT: 
	        			tempUser = selectedAuditUser;
	        			if(tempUser.getUsername().compareTo(msg.getUsername()) != 0) {
	        				break;
	        			}
	        			tempUser.addChat(msg.getChat().getCopy());
	        			updateChatList(tempUser);
	        			break;
	        		case SubType.SEND_TEXT_MESSAGE: 
	        			tempUser = selectedAuditUser;
	        			if(tempUser.getUsername().compareTo(msg.getUsername()) != 0)
	        				break;
	            	tempUser.addMessageToChat(msg.getChatId(), msg.getTextMessage());
	            	updateChatList(tempUser);
	        			break;
	        	}
	        }
        	else if(msg.mainType == MainType.CHAT_OPERATION) {
        		switch (msg.getSubType()) {
	        		case SubType.ADD_USER_TO_GC:
	        			user.addChat(msg.getChat());
	        			updateChatList(user);
	        			break;
	        		case SubType.REMOVE_USER_FROM_GC:
	        			user.removeChat(msg.getChatId(), msg.getUsername());
	        			updateChatList(user);
	        			break;
        		}
        	}
	        else { 
	            	/*if(msg.getUser() != null)
	            		System.out.println("\n" + msg.getUser().getUsername() + ": " + msg.getText() + '\n'); //display message along with who its from
	            	else {
	            		System.out.println("\nServer: " + msg.getText() + '\n');
	            	}*/
	            }
        }
	}

	//helper functions
	public void sendToServer(Message message) throws IOException{
		objectOutputStream.writeObject(message);
	}
	
	public void updateMessageHistory(Message message) {
		messageHistory.add(message);
	}
	
    // MESSAGE: MainType.AUTHENTICATION
    // SubType.LOGIN
	public User login(User user) throws IOException, ClassNotFoundException {
        System.out.println(user.getUsername() + " attempting to log in...");
        
        Message loginRequestMessage = new Message(MainType.AUTHENTICATION, SubType.LOGIN, Status.REQUEST, user.getUsername() + "requesting login", user); //login message created
        updateMessageHistory(loginRequestMessage); //add the login message to the message history
        sendToServer(loginRequestMessage); //sending the login message to server
        
        
        Message incomingLoginResponse = (Message) objectInputStream.readObject(); //deSerialized the message
        updateMessageHistory(incomingLoginResponse);

        if(incomingLoginResponse.mainType == MainType.SERVER && incomingLoginResponse.subType == SubType.EXIT) {
        	gui.forceExit();
        	return null;
				} else if(incomingLoginResponse.status == Status.SUCCESS && incomingLoginResponse.subType == SubType.LOGIN_RESPONSE) {
            System.out.println(incomingLoginResponse.getText() + "\n");
            //System.out.println("Enter text to send!\n");
            User actualUser = incomingLoginResponse.getUser();
            actualUser.addChatThreadSafety();
            this.user = actualUser;
            return actualUser;
        } else if (incomingLoginResponse.status == Status.INVALID) {
        	System.out.println("User Already Online");
        	return null;
				} else {
        	System.out.println("Invalid Login. Please try again.");
        	return null;
        }
	}
	
    // SubType.LOGOUT
	public void logout() throws IOException, ClassNotFoundException {
		Message logOutRequest = new Message(MainType.AUTHENTICATION, SubType.LOGOUT , Status.REQUEST, "", user.getUsername());
		updateMessageHistory(logOutRequest); //store operation in history
		sendToServer(logOutRequest);
	}
	
	// SubType.CREATE_USER
	public boolean createNewAccount(User user) throws IOException, ClassNotFoundException {
		Message createAccountRequest = new Message(MainType.AUTHENTICATION, SubType.CREATE_USER , Status.REQUEST, user.getUsername() + "attempting to create account...\n", user);
		updateMessageHistory(createAccountRequest); //store operation in history
		sendToServer(createAccountRequest);
		
		Message incomingAccountCreationResponse = (Message) objectInputStream.readObject(); //deSerialized the message
        updateMessageHistory(incomingAccountCreationResponse);
        
        if(incomingAccountCreationResponse.status == Status.SUCCESS && incomingAccountCreationResponse.subType == SubType.CREATE_USER) {
        	// account was made successfully
        	return true;
        }
        else {
        	// failed to make account
        	return false;
        }
	}
	
	// MESSAGE: MainType.TEXT
	// SubType.SEND_TEXT_MESSAGE
	public void sendMessage(String text, int chatId) throws IOException {
	    Message message = new Message(MainType.TEXT, SubType.SEND_TEXT_MESSAGE , Status.REQUEST, text, user.getUsername(), chatId);
	    updateMessageHistory(message); //the message the user input should be sent
	    sendToServer(message); //where the object gets serialized and sent     
	}

	
	
	// MESSAGE: MainType.DISPLAY
	// SubType.ACTUAL_CHAT
	public void requestActualChat() throws IOException, ClassNotFoundException {
		Message actualChatRequest = new Message(MainType.DISPLAY, SubType.ACTUAL_CHAT , Status.REQUEST, null, user);
		updateMessageHistory(actualChatRequest); //store operation in history
		sendToServer(actualChatRequest);

	}
	
	// SubType.USER_STATE
	public void getUserState() throws IOException, ClassNotFoundException {
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
	public void addUserToChat(String userToAdd, int chatID) throws IOException {
		Message addUserToGC = new Message(MainType.CHAT_OPERATION, SubType.ADD_USER_TO_GC , Status.REQUEST, userToAdd, user.getUsername(), chatID);
		updateMessageHistory(addUserToGC); //store operation in history
		sendToServer(addUserToGC);
	}
	
	// SubType.REMOVE_USER_FROM_GC
	public void removeUserFromChat(String userToRemove, int chatID) throws IOException {
		Message removeUserFromGC = new Message(MainType.CHAT_OPERATION, SubType.REMOVE_USER_FROM_GC , Status.REQUEST, userToRemove, user.getUsername(), chatID);
		updateMessageHistory(removeUserFromGC); //store operation in history
		sendToServer(removeUserFromGC);
	}
	
	// SubType.DELETE_GC
	public void DeleteChat(String chatID) throws IOException {
		//need a chatID to perform, most likely the chat were hovering over/clicking on
		Message chatToDelete = new Message(MainType.CHAT_OPERATION, SubType.DELETE_GC , Status.REQUEST, chatID, user.getUsername());
		updateMessageHistory(chatToDelete); //store operation in history
		sendToServer(chatToDelete);
	}	
		
	
	
	// MESSAGE: MainType.AUDIT_OPERATION
	// SubType.ENTER_AUDIT_MODE
	/*public void enterAuditMode() throws IOException {
		Message enterAuditMode = new Message(MainType.AUDIT_OPERATION, SubType.ENTER_AUDIT_MODE , Status.REQUEST, null, user);
		updateMessageHistory(enterAuditMode); //store operation in history
		sendToServer(enterAuditMode);
	}*/

	/*public void auditResponse(boolean isIt) {
		if(!isIt)
			return;
		//Gui Logic for switching to audit view goes here
		//
		//
	}*/
	
	// SubType.SELECT_USER
	public void audit_SelectUser(String username) throws IOException {
		Message selectedUser = new Message(MainType.AUDIT_OPERATION, SubType.SELECT_USER , Status.REQUEST, username, user.getUsername());
		updateMessageHistory(selectedUser); //store operation in history
		sendToServer(selectedUser);
	}
	
	// SubType.VIEW_CHATS
	public void audit_ViewChats() throws IOException {
		Message viewChatsRequest = new Message(MainType.AUDIT_OPERATION, SubType.VIEW_CHATS , Status.REQUEST, null, user);
		updateMessageHistory(viewChatsRequest); //store operation in history
		sendToServer(viewChatsRequest);
	}
	
	
	private void updateChatList(User inputUser) {
     inputUser.addChatThreadSafety();
     SwingUtilities.invokeLater(() -> gui.reloadChatList(inputUser));
	}

	public User getUser() {
		return user;
	}


}
