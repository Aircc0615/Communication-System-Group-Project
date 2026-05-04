package GUI;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.*;

import chat.Chat;
import chat.TextMessage;
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
	private JButton chatHeaderButton;
	JMenuItem addUserItem;
	JMenuItem removeUserItem;
	JMenuItem deleteChatItem;
	
	
	public GUI(Client client) throws UnknownHostException, IOException{
		 this.client = client;
		 client.assignGUI(this);
		 currentChatId = -1;
		 client.connectToServer(); //as soon as the application runs we connect to the server
		 buildGUI();
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
		 loginFrame.setResizable(false);
		 
	 }
	 public void createLoginForm() throws UnknownHostException, IOException {
		 JLabel userLabel = new JLabel("Username");
		 JTextField usernameField = new JTextField(16);
		 
		 JLabel passLabel = new JLabel("passsword");
		 JPasswordField passwordField = new JPasswordField(16);
		 
		 JButton submitB = new JButton("Login");
		 JButton createNewAccountBtn = new JButton("Create Account");
		 JCheckBox itAccountCheckBox = new JCheckBox("Create as IT account");
		 
		 
		 JLabel welcomeLabel = new JLabel("Welcome", SwingConstants.CENTER);
		 welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
		 welcomeLabel.setBackground(new Color(0xD3E3E3));
		 welcomeLabel.setForeground(Color.BLACK);
		 welcomeLabel.setOpaque(true);
		 
		 
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
		 
		 createNewAccountBtn.addActionListener(e -> {
			    String username = usernameField.getText();
			    String password = new String(passwordField.getPassword());
			    boolean isIT = itAccountCheckBox.isSelected();

			    user = new User(username, password, isIT);
			    try {
					if(createNewAccount()) { //if the account was made successfully
						JOptionPane.showMessageDialog(loginFrame, 
								"Your account was succesfully created. Please try logging in!",
								"Account Succesfully Created",
								JOptionPane.DEFAULT_OPTION);
					}
					else { //failed to make the account
						JOptionPane.showMessageDialog(loginFrame, 
								"Please provide a unique username. Username and password must be a minimum of 6 characters in length.",
								"Account Creation Failed",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (IOException | ClassNotFoundException e1 ) {
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
	     
	     JPanel buttonPanel = new JPanel();
	     buttonPanel.setLayout(new GridLayout(2, 1, 0, 8));
	     buttonPanel.add(submitB);
	     buttonPanel.add(createNewAccountBtn);
	     buttonPanel.add(itAccountCheckBox);
	     
	     
	     //combine login
	     JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
	     mainPanel.add(formPanel, BorderLayout.CENTER);
	     mainPanel.add(buttonPanel, BorderLayout.SOUTH);
	    

	     JPanel centerPanel = new JPanel(new GridBagLayout());
	     centerPanel.add(mainPanel);
	     
	     //loginFrame.add(topPanel, BorderLayout.NORTH);
	     loginFrame.add(centerPanel, BorderLayout.CENTER);
		 
	 }
	
	 
	 public void createMainFrame() {
		 mainFrame = new JFrame();
		 
		 mainFrame.setSize(750, 700);
		 mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		 mainFrame.addWindowListener(new WindowAdapter() {
			 public void windowClosing(WindowEvent event) {
				 try {
					 logoutUser();
					 Thread.sleep(300);
				 } catch(Exception e) {
					 e.printStackTrace();
				 }
			 
				 mainFrame.dispose();
				 System.exit(0);
			 }
		 });
		 
		 //mainFrame.setLocationRelativeTo(null);
		 mainFrame.setResizable(false);
		 
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
		
		
		 if(auditMode == true) {
			 JLabel selectUserLabel = new JLabel("                  Select User" );
			
			 selectUserLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			 selectUserLabel.setOpaque(true); 
			 selectUserLabel.setBackground(new Color(163, 177, 138));
			 selectUserLabel.setPreferredSize(new Dimension(240, 40));
			 selectUserLabel.setMaximumSize(selectUserLabel.getPreferredSize());
			
			 selectUserLabel.addMouseListener(new MouseAdapter() {
			        public void mouseClicked(MouseEvent e) {
			            String username = JOptionPane.showInputDialog(
			                mainFrame,
			                "Enter username:",
			                "Select User",
			                JOptionPane.PLAIN_MESSAGE
			            );
			            if (username != null && !username.trim().isEmpty()) {
			                try {
			                    audit_SelectUser(username.trim());
			                } catch (IOException ex) {
			                    ex.printStackTrace();
			                }
			            }
			        }
			    });
			
			 addChatPanel.add(selectUserLabel);
		 }else {
			 JLabel newChatLabel = new JLabel("               Create New Chat" );
			 newChatLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			 //newChatLabel.setOpaque(true); 
			 newChatLabel.setPreferredSize(new Dimension(240, 40));
			 newChatLabel.setMaximumSize(newChatLabel.getPreferredSize());
			 
			 newChatLabel.setBackground(new Color(0xD3E3E3));
			 newChatLabel.setForeground(Color.BLACK);
			 newChatLabel.setOpaque(true);
			
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
		 }
		
		
		 
		 //middle
		 JScrollPane optionScrollPane = new JScrollPane();		
		 optionScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));	
		 optionScrollPane.setPreferredSize(new Dimension(240, 530));
		 
		 //display chatList
		 reloadChatList(user);
		 
		 optionScrollPane.setViewportView(chatListPanel);
		 

		 //bottom
		 JPanel leftBottomPane = new JPanel();
		 leftBottomPane.setPreferredSize(new Dimension(240, 50));
		 leftBottomPane.setBackground(new Color(0xD3E3E3));
		 leftBottomPane.setForeground(Color.BLACK);
		 leftBottomPane.setOpaque(true);
		 
		 JPanel textPanel = new JPanel();
	     textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
	     textPanel.setPreferredSize(new Dimension(130, 40));
	     textPanel.setOpaque(false);
	     
		 JLabel pic = new JLabel();
	     pic.setPreferredSize(new Dimension(40, 30));
	     pic.setBackground(new Color(0x548282));
		 pic.setForeground(Color.BLACK);
		 pic.setOpaque(true);
	     
	     JLabel nameLabel = new JLabel(user.getUsername());	//pass in users name
	     nameLabel.setForeground(Color.BLACK);
	     nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

	     JLabel subLabel = new JLabel("view profile");
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
		 JPanel topRightPanel = new JPanel(new BorderLayout());
		 topRightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		 topRightPanel.setPreferredSize(new Dimension(500, 50));
		 topRightPanel.setBackground(new Color(0xD3E3E3));
		 topRightPanel.setForeground(Color.BLACK);
		 topRightPanel.setOpaque(true);

		 chatHeaderButton = new JButton("Select a chat");
		 chatHeaderButton.setFocusPainted(false);
		 chatHeaderButton.setEnabled(false);

		 JPopupMenu chatMenu = new JPopupMenu();

		 addUserItem = new JMenuItem("Add people to chat");
		 removeUserItem = new JMenuItem("Remove user from chat");
		 deleteChatItem = new JMenuItem("Delete chat");

		 chatMenu.add(addUserItem);
		 chatMenu.add(removeUserItem);
		 chatMenu.addSeparator();
		 chatMenu.add(deleteChatItem);

		 chatHeaderButton.addActionListener(e -> {
		     chatMenu.show(chatHeaderButton, 0, chatHeaderButton.getHeight());
		 });

		 addUserItem.addActionListener(e -> {
		     String username = JOptionPane.showInputDialog(mainFrame, "Enter username to add:");
		     if (username != null && !username.trim().isEmpty()) {
		         try {
		             addUserToChat(username.trim(), currentChatId);
		             reloadChatList(user);
		             JOptionPane.showMessageDialog(mainFrame, "User added to chat.");
		         } catch (IOException ex) {
		             JOptionPane.showMessageDialog(mainFrame, "Failed to add user.");
		             ex.printStackTrace();
		         }
		     }
		 });

		 removeUserItem.addActionListener(e -> {
		     String username = JOptionPane.showInputDialog(mainFrame, "Enter username to remove:");
		     if (username != null && !username.trim().isEmpty()) {
		         try {
		             removeUserFromChat(username.trim(), currentChatId);
		             reloadChatList(user);
		             JOptionPane.showMessageDialog(mainFrame, "User removed from chat.");
		         } catch (IOException ex) {
		             JOptionPane.showMessageDialog(
		                 mainFrame,
		                 "Failed to remove user. Only the chat creator can remove people."
		             );
		             ex.printStackTrace();
		         }
		     }
		 });

		 deleteChatItem.addActionListener(e -> {
			    if (currentChatId == -1) {
			        JOptionPane.showMessageDialog(mainFrame, "No chat selected.");
			        return;
			    }

			    int confirm = JOptionPane.showConfirmDialog(
			        mainFrame,
			        "Delete chat " + currentChatId + "?",
			        "Confirm Delete",
			        JOptionPane.YES_NO_OPTION
			    );

			    if (confirm == JOptionPane.YES_OPTION) {
			        try {
			            deleteGroupChat(String.valueOf(currentChatId));

			            currentChatId = -1;
			            chatHeaderButton.setText("Select a chat");

			            panel1.removeAll();
			            panel1.revalidate();
			            panel1.repaint();

			            reloadChatList(user);
			            
			            JOptionPane.showMessageDialog(mainFrame, "Chat deleted.");

			        } catch (IOException ex) {
			            JOptionPane.showMessageDialog(mainFrame, "Failed to delete chat.");
			            ex.printStackTrace();
			        }
			    }
			});

		 topRightPanel.add(chatHeaderButton, BorderLayout.CENTER);
		 
		 //middle
		 msgScrollPane = new JScrollPane();
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
			 
			 exportButton.addActionListener(e -> {
				 try {
					 audit_ExportChatLog();
				 } catch (IOException e1) {
					 e1.printStackTrace();
				 }
			 });
			 
			 System.out.println("enter audit mode");
		 }else {
			 JTextField inputField = new JTextField();
			 JButton sendButton = new JButton("Send");
			 sendButton.setBackground(new Color(0xD3E3E3));
			 sendButton.setForeground(Color.BLACK);
			 sendButton.setOpaque(true);
			    
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
		 try {
		 Chat chat = user.getCopyOfChat(chatId);
		 for(int i = 0; i < chat.getNumMessages(); i++) {
			 
			    JPanel msgPanel = new JPanel(new BorderLayout());

			    TextMessage msg = chat.getMessage(i);

			    JTextArea textArea = new JTextArea(msg.getText());
			    textArea.setBorder(BorderFactory.createTitledBorder(msg.getUsername() + " - " + msg.getTimeSent()));	

			    textArea.setLineWrap(true);
			    textArea.setWrapStyleWord(true);
			    textArea.setEditable(false);
			    textArea.setBackground(new Color(0xD3E3E3));
			    textArea.setForeground(Color.BLACK);
			    textArea.setOpaque(true);

			    textArea.setColumns(15);  
			    textArea.setSize(textArea.getPreferredSize());
			    msgPanel.setMaximumSize(msgPanel.getPreferredSize());

			    if(msg.getUsername().equals(user.getUsername())) {
			        msgPanel.add(textArea, BorderLayout.EAST);
			    } else {
			        msgPanel.add(textArea, BorderLayout.WEST);
			    }

			    msgPanel.setMaximumSize(
			        new Dimension(Integer.MAX_VALUE, msgPanel.getPreferredSize().height)
			    );

			    panel1.add(msgPanel);
			}
		 }catch (Exception e) {
			 return;
		 }
		 panel1.revalidate();
		 panel1.repaint();
		 scrollToBottom();
		 
	}

	private void scrollToBottom(){
		if (msgScrollPane == null){
			return;
		}
		SwingUtilities.invokeLater(() ->{
			JScrollBar verticalBar = msgScrollPane.getVerticalScrollBar();
			verticalBar.setValue(verticalBar.getMaximum());
		});
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
			mainFrame.dispose();
			createMainFrame();
		});
		
		exitAudit.addActionListener(e -> {
			auditMode = false;
			user = client.getUser();
			mainFrame.dispose();
			createMainFrame();
		});
		
		logout.addActionListener(e -> {
			auditMode = false;
			try {
				logoutUser();
				mainFrame.dispose();
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
			}
		
		});
		
		//profile.add(myProfile);
			if(auditMode == true) {
	    		profile.add(exitAudit);
	    		profile.addSeparator();
			} else {
				if(user.isInformationTechnologyUser()) {
	    		profile.add(audit);
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
					e1.printStackTrace();
				 }
			 }
		});
		 
		 chatOption.add(CreateChat);
		 
		 return chatOption;
	 }
	 private void setCurrentChat(int chatId) {
		    currentChatId = chatId;
		    chatHeaderButton.setText("Chat ID: " + chatId);
		    chatHeaderButton.setEnabled(true);
		    
		    boolean isCreator = isCurrentUserCreator(chatId);

		    addUserItem.setVisible(isCreator);
		    removeUserItem.setVisible(isCreator);
		    deleteChatItem.setVisible(isCreator);
		}
	 
	 private boolean isCurrentUserCreator(int chatId) {
		 		Chat chat = null;
		 		try {
		 			chat = user.getChatList().getCopyOfChat(chatId);
		 		} catch (IndexOutOfBoundsException e) {
		 			return false;
		 		}

		    if (chat == null) {
		        return false;
		    }

		    return chat.getCreatorUsername().equals(user.getUsername());
	}
	 
	 public void reloadChatList(User user) {
		 if(this.user != user) {
			 System.out.println("User is not active gui user");
			 return;
		 }
		 if(chatListPanel == null) {
		 		chatListPanel = new JPanel();
		 		chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
		 }
		 chatListPanel.removeAll();
		 //chatListPanel.setBorder(BorderFactory.createTitledBorder("right Panel"));
		 
		 int[] chatIds = user.getChatIds();
		 int groupNumber = 1;
		 
		 for (int i = 0; i < chatIds.length; i++) {
		        int chatId = chatIds[i];
		        Chat chat;
		        try {
		        	chat = user.getCopyOfChat(chatId);
		        } catch(IndexOutOfBoundsException e) {continue;}

		        ArrayList<String> otherUsers = new ArrayList<>();
		        String[] memberUsernames = chat.getMembersInChat();

		        for (int j = 0; j < chat.getNumMembers(); j++) {
		            String name = memberUsernames[j];

		            if (!name.equals(user.getUsername()) && !otherUsers.contains(name)) {
		                otherUsers.add(name);
		            }
		        }

		        String chatName;

		        if (otherUsers.size() == 1) {
		            chatName = otherUsers.get(0);
		        } else if (otherUsers.size() > 1) {
		            chatName = "Group " + chatId;
		            groupNumber++;
		        } else {
		            chatName = "Chat " + chatId;
		        }
		        
			    JButton chatButton = new JButton(chatName);
			    chatButton.setMaximumSize(new Dimension(240, 40));
			    chatButton.setBackground(new Color(0xD3E3E3));
			    chatButton.setForeground(Color.BLACK);
			    chatButton.setOpaque(true);
			    chatButton.setContentAreaFilled(true);
			    chatButton.setBorderPainted(true);
			    chatButton.setFocusPainted(false);

			    chatButton.addActionListener(e -> {
			        setCurrentChat(chatId);
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
			 JOptionPane.showMessageDialog(loginFrame, "Login failed! Invalid username/password, or the User is already online!");
		 }

		 
	 }

	 public void setNewAuditUser(User user) {
		 if(user != null) {
			 this.user = user;
			 System.out.println("Auditing user: " + user.getUsername());
			 mainFrame.dispose();
			 createMainFrame();
		 }
	 }
	 
	 
	 // SubType.LOGOUT
	 private void logoutUser() throws ClassNotFoundException, IOException {
		 client.logout();
	 }

	 public void forceExit() {
		 JOptionPane.showMessageDialog(null, "Server Closed.");
		 System.exit(0);
	 }
	 
	// SubType.CREATE_USER
	 public boolean createNewAccount() throws IOException, ClassNotFoundException {
		 return client.createNewAccount(user);
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
	private void addUserToChat(String username, int chatID) throws IOException {
		 client.addUserToChat(username, chatID);
	 }
	 
	// SubType.REMOVE_USER_FROM_GC
	 private void removeUserFromChat(String username, int chatID) throws IOException {
		 client.removeUserFromChat(username, chatID);
	 }
	 
	// SubType.DELETE_GC
	 private void deleteGroupChat(String chatID) throws IOException { //the GUI needs to handle retrieve the chatID
		 client.DeleteChat(chatID);
	 }
		
	// MESSAGE: MainType.AUDIT_OPERATION
	// SubType.ENTER_AUDIT_MODE
	 /*private void enterAuditMode() throws IOException {
		 client.enterAuditMode();
	 }*/
	 
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
		 if(currentChatId == -1)
			 return;
		 user.exportChat(currentChatId, false);
	 }
	 
}