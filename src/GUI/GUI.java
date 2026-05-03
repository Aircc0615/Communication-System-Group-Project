package GUI;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.*;

import chat.Chat;
import networking.Client;
import user.User;



public class GUI {
	JFrame loginFrame;
	JFrame mainFrame;
	boolean isLogged = false;
	boolean auditMode = false;
	User user;
	Client client;
	private int currentChatId;
	JScrollPane msgScrollPane; 
	JTextArea textArea;
	JPanel panel1;
	private JPanel chatListPanel;
	
	public GUI(Client client) throws UnknownHostException, IOException{
		 buildGUI();
		 this.client = client;
		 client.assignGUI(this);
		 currentChatId = -1;
	 }
	
	 public void buildGUI() throws UnknownHostException, IOException {
		 createLoginFrame();
	 }
	 
	 public void createLoginFrame() throws UnknownHostException, IOException {
		 loginFrame = new JFrame();
		 
		 //set frame size
		 loginFrame.setSize(450, 700);
		 loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 
		 createLoginForm();
		 
		 //make frame visible
		 loginFrame.setVisible(true);
		 
	 }
	 public void createLoginForm() throws UnknownHostException, IOException {
		 client.connectToServer(); //as soon as the application runs we connect to the server
		 
		 JLabel userLabel = new JLabel("Username");
		 JTextField usernameField = new JTextField(16);
		 
		 JLabel passLabel = new JLabel("passsword");
		 JPasswordField passwordField = new JPasswordField(16);
		 
		 JButton submitB = new JButton("Login");
		 
		 JLabel welcomeLabel = new JLabel("Welcome", SwingConstants.CENTER);
		 welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
		 
		 
		 submitB.addActionListener(e -> {
			    String username = usernameField.getText();
			    String password = new String(passwordField.getPassword());
			    user = new User(username, password);
			    //System.out.println(username + "\n"+ password);
			    try {
					login();
					
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
				}
			});
		 
		 //layout
		 JPanel formPanel = new JPanel();
	     formPanel.setLayout(new GridLayout(5, 1, 0, 1));
	     formPanel.add(welcomeLabel);
	     formPanel.add(userLabel);
	     formPanel.add(usernameField);
	     formPanel.add(passLabel);
	     formPanel.add(passwordField);
	     
	     //combine login
	     JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
	     mainPanel.add(formPanel, BorderLayout.CENTER);
	     mainPanel.add(submitB, BorderLayout.SOUTH);

	     JPanel centerPanel = new JPanel(new GridBagLayout());
	     centerPanel.add(mainPanel);
	     
	     //loginFrame.add(topPanel, BorderLayout.NORTH);
	     loginFrame.add(centerPanel, BorderLayout.CENTER);
		 
	 }
	
	 
	 public void createMainFrame() {
		 mainFrame = new JFrame();
		 
		 mainFrame.setSize(750, 700);
		 mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 //mainFrame.setLocationRelativeTo(null);
		 
		 createLeftMainPanel();
		 createRightMainPanel();
		 mainFrame.setVisible(true);
		 
		 mainFrame.revalidate();
		 mainFrame.repaint();
		 
	 }
	 
	 private void createLeftMainPanel() {
		 JPanel leftPanel = new JPanel();
		 leftPanel.setPreferredSize(new Dimension(250, 0));
		 leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		 //top
		 
		 JPanel addChatPanel = new JPanel();
		 addChatPanel.setPreferredSize(new Dimension(240, 50));
		 addChatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		 addChatPanel.setLayout(new BoxLayout( addChatPanel, BoxLayout.X_AXIS));
		 
		 
		 
		 JLabel newChatLabel = new JLabel("               Create New Chat" );
		 
		 
		 newChatLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		 newChatLabel.setOpaque(true);  
		 newChatLabel.setBackground(new Color(163, 177, 138));
		 newChatLabel.setPreferredSize(new Dimension(240, 40));
		 newChatLabel.setMaximumSize(newChatLabel.getPreferredSize());
		 
		 newChatLabel.addMouseListener(new MouseAdapter() {
			 public void mouseClicked(MouseEvent e) {
				 try {
					createNewChatOption().show(addChatPanel, e.getX(), e.getY());
				 } catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				 }
			    }
		 });
		 
		 
		 
		 addChatPanel.add(newChatLabel);
		 
		 
		 
		 
		 //middle
		 JScrollPane optionScrollPane = new JScrollPane();		//hold all the people user had message
		 optionScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));	//for testing
		 optionScrollPane.setPreferredSize(new Dimension(240, 530));
		 
		 //display chatList
		 reloadChatList();
		 
		 optionScrollPane.setViewportView(chatListPanel);
		 

		 //bottom
		 JPanel leftBottomPane = new JPanel();
		 leftBottomPane.setPreferredSize(new Dimension(240, 50));
		 leftBottomPane.setBackground(new Color(163, 177, 138));
		 
		 JPanel textPanel = new JPanel();
	     textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
	     textPanel.setPreferredSize(new Dimension(130, 40));
	     textPanel.setOpaque(false);
	     
	     
		 JLabel pic = new JLabel();
	     pic.setPreferredSize(new Dimension(40, 30));
	     pic.setBackground(new Color(255, 192, 203));
	     pic.setOpaque(true);
	     
	     JLabel nameLabel = new JLabel(user.getUsername());	//pass in users name
	     nameLabel.setForeground(Color.WHITE);
	     nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

	     JLabel subLabel = new JLabel("view pfp");
	     subLabel.setForeground(Color.GRAY);
	     subLabel.setFont(subLabel.getFont().deriveFont(12f));
	     
	     textPanel.add(nameLabel);
	     textPanel.add(subLabel);
	     
	     leftBottomPane.add(pic, BorderLayout.WEST);
	     leftBottomPane.add(textPanel, BorderLayout.CENTER);

	     leftBottomPane.addMouseListener(new MouseAdapter() {
	    	 public void mouseClicked(MouseEvent e) {
	    		 createProfileMenu().show(leftBottomPane, e.getX(), e.getY());
	    	 }

			 
	     });
	     
	     
	     
	     
	     
	     leftPanel.add(addChatPanel);
		 leftPanel.add(optionScrollPane);
		 leftPanel.add(leftBottomPane);
		 
		 
		 
		 //adding to main frame
		 mainFrame.add(leftPanel, BorderLayout.WEST);
	     
		 
	 }
	 
	 private void createRightMainPanel() {
		 JPanel rightPanel = new JPanel();
		 rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		 rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS ));
		 
		 
		 //top
		 JPanel topRightPanel = new JPanel();
		 //topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.Y_AXIS));
		 topRightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		 topRightPanel.setPreferredSize(new Dimension(500, 50));
		 
		 //middle
		 JScrollPane msgScrollPane = new JScrollPane();
		 msgScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		 msgScrollPane.setPreferredSize(new Dimension(10, 700));
		 
		 panel1 = new JPanel();
		 panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
		 //panel1.setBorder(BorderFactory.createTitledBorder("Panel1"));
         
		 
		 
		 msgScrollPane.setViewportView(panel1);
		 
		 JPanel rightBottomPanel = new JPanel(new BorderLayout());
		 
		 //bottom
		 if(auditMode == true) {
			 JButton exportButton = new JButton("Export");
			 rightBottomPanel.add(exportButton);
			 System.out.println("enter audit mode");
		 }else {
			 JTextField inputField = new JTextField();
			 JButton sendButton = new JButton("Send");
			 sendButton.addActionListener(e -> {
				 try {
					handleSendMessage(inputField.getText());
				 } catch (IOException e1) {
					e1.printStackTrace();
				 }
				 inputField.setText("");
			 });

			 rightBottomPanel.add(inputField, BorderLayout.CENTER);
			 rightBottomPanel.add(sendButton, BorderLayout.EAST);
		 }
		 
		 rightPanel.add(topRightPanel);
		 rightPanel.add(msgScrollPane);
		 rightPanel.add(rightBottomPanel);
		 
		 mainFrame.add(rightPanel, BorderLayout.CENTER);
	 }
	 
	 
	 
	 public void displayChat(int chatId) {
		 panel1.removeAll();
		 currentChatId = chatId;
		 Chat chat = user.getCopyOfChat(chatId);
		 
		 for(int i = 0; i< chat.getNumMessages(); i++) {
			 
			 JPanel msgPanel = new JPanel(new BorderLayout());
		 
			 JTextArea textArea = new JTextArea(chat.getMessage(i).getText());
			 textArea.setBorder(BorderFactory.createTitledBorder(chat.getMessage(i).getUsername()));	
			 textArea.setLineWrap(true);
			 textArea.setWrapStyleWord(true);
			 textArea.setEditable(false);
			 
			 textArea.setColumns(15);  
			 //textArea.setRows(5);
			 textArea.setSize(textArea.getPreferredSize());
			 msgPanel.setMaximumSize(msgPanel.getPreferredSize());
			 
			 if(chat.getMessage(i).getUsername().equals(user.getUsername())) {
				 msgPanel.add(textArea, BorderLayout.EAST);
				 
			 }else {
				 msgPanel.add(textArea, BorderLayout.WEST);
				 
				 
			 }
			 
			 msgPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, msgPanel.getPreferredSize().height));
			 //msgPanel.setMaximumSize(msgPanel.getPreferredSize());
			 panel1.add(msgPanel);
		 }
		 panel1.revalidate();
		 panel1.repaint();
		 
	}
	
	 private JPopupMenu createProfileMenu() {
		JPopupMenu profile = new JPopupMenu();
		//JMenuItem myProfile = new JMenuItem("My Profile"); //add if have time
	    JMenuItem audit = new JMenuItem("Audit Mode");
	    JMenuItem exitAudit = new JMenuItem("Exit Audit Mode");
	    JMenuItem logout = new JMenuItem("Logout");
	    
		//myProfile.addActionListener(e -> openProfilePage());
		audit.addActionListener(e -> {
			auditMode = true;
			createMainFrame();
		});
		
		exitAudit.addActionListener(e -> {
			auditMode = false;
			createMainFrame();
		});
		
		logout.addActionListener(e -> {
			auditMode = false;
			try {
				logoutUser();
				mainFrame.setVisible(false);
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		});
		
		//profile.add(myProfile);
	    if(user.getRole().equals("IT")) {
	    	if(auditMode == false) {
	    		profile.add(audit);
			    profile.addSeparator();
	    	}else {
	    		profile.add(exitAudit);
	    		profile.addSeparator();
	    	}
	    }
	    profile.add(logout);
	    
		
		return profile;
	}
	 private JPopupMenu createNewChatOption() throws IOException {
		 JPopupMenu chatOption = new JPopupMenu();
		 JMenuItem CreateChat = new JMenuItem("Create new chat!");
		 
		 CreateChat.addActionListener(e -> {
			 //input popup that allows user to input who to send to
			 String usernames = JOptionPane.showInputDialog(mainFrame, "Enter usernames serparated by commas: "); //whatever the user inputs needs to be stored here
			 if(usernames != null) {
				 try {
					handleCreateChat(usernames);
				 } catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				 }
			 }
		});
		 
		 chatOption.add(CreateChat);
		 
		 return chatOption;
	 }
	 
	 
	 public void reloadChatList() {
		 if(chatListPanel == null) {
		 		chatListPanel = new JPanel();
		 		chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
		 		System.out.println("First load of chat list");
		 } else {System.out.println("Reloading chat list due to update");}
		 chatListPanel.removeAll();
		 //chatListPanel.setBorder(BorderFactory.createTitledBorder("right Panel"));
		 
		 int[] chatIds = user.getChatIds();
		 for (int i = 0; i < user.getChatList().getNumChats(); i++) {
			    //final Chat currentChat = chat;
			 		int chatId = chatIds[i];
			    JButton chatButton = new JButton("Chat " + chatId);
			    chatButton.setMaximumSize(new Dimension(220, 40));

			    chatButton.addActionListener(e -> {
			    displayChat(chatId);
			    
			    });
			    chatListPanel.add(chatButton);
		 }
		 if(currentChatId != -1) {
			 displayChat(currentChatId);
		 }
		 chatListPanel.revalidate();
		 chatListPanel.repaint();
	 }
	 
	 // CLIENT OPERATIONS
	 
    // MESSAGE: MainType.AUTHENTICATION
    // SubType.LOGIN
	 public void login() throws ClassNotFoundException, IOException { 
		 User authenticatedUser = client.login(user); //client will send login message to server
		 
		 if(authenticatedUser != null) { // server returns a response either a null value or user
			 this.user = authenticatedUser;
			 loginFrame.dispose();
			 client.createServerListener();
			 createMainFrame();
		 }
		 else {
			 JOptionPane.showMessageDialog(loginFrame, "Login failed! Invalid username/password.");
		 }

		 
	 }
	 
	 
	 
	 
	 // SubType.LOGOUT
	 private void logoutUser() throws ClassNotFoundException, IOException {
		 client.logout();
	 }
	 	 
	 // MESSAGE: MainType.TEXT
	 // SubType.SEND_TEXT_MESSAGE
	 //GUI needs a way to know which chat were referencing. 
	 private void handleSendMessage(String messageToSend) throws IOException {
		 if(currentChatId == -1)
			 return;
		 client.sendMessage(messageToSend, currentChatId); //user should not have to pass the chatID, this needs to be done for the user
	 }
	 
	 // MESSAGE: MainType.DISPLAY
	 // SubType.ACTUAL_CHAT
	 private void getActualChat() throws ClassNotFoundException, IOException{
		 // TO-DO: needs implementation on client side
		 client.requestActualChat();
	 }
	 
	 // SubType.USER_STATE
	 private void getUserState() throws ClassNotFoundException, IOException {
		 client.getUserState();
	 }
	 
	
	 // user will pass usernames, server takes care of converting single string to array
	 // example argument for function: John, Henry, Kevin, Kayla, Jennifer, Michael
	 // user can also pass single name, example: John
	 // server takes care of constructing group chat or private chat
	 
	 // MESSAGE: MainType.CHAT_OPERATIONs
	 // CREATE_GC 	||       this will work for making either a DM or GC	 
	 private void handleCreateChat(String usernames) throws IOException {
		 client.createChat(usernames); 
	 }
	
	// SubType.ADD_USER_TO_GC
	private void addUserToChat(String username) throws IOException {
		 client.addUserToChat(username);
	 }
	 
	// SubType.REMOVE_USER_FROM_GC
	 private void removeUserFromChat(String username) throws IOException {
		 client.removeUserFromChat(username);
	 }
	 
	// SubType.DELETE_GC
	 private void deleteGroupChat(String chatID) throws IOException { //the GUI needs to handle retrieve the chatID
		 client.DeleteChat(chatID);
	 }
		
	// MESSAGE: MainType.AUDIT_OPERATION
	// SubType.ENTER_AUDIT_MODE
	 private void enterAuditMode() throws IOException {
		 client.enterAuditMode();
	 }
	 
	 // SubType.SELECT_USER
	 private void audit_SelectUser(String username) throws IOException{
		 client.audit_SelectUser(username); 
	 }
	 
	 // SubType.VIEW_CHATS
	 private void audit_ViewChats() throws IOException {
		client.audit_ViewChats();
	}
	
	 // SubType.EXPORT_CHAT_LOG
	 private void audit_ExportChatLog() throws IOException {
		client.audit_ExportChatLog();
	 }
	 
}