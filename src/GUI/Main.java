package GUI;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;

import chat.Chat;
import chat.ChatType;
import chat.TextMessage;
import networking.Client;
import user.User;

public class Main {

	public static void main(String[] args) throws UnknownHostException, IOException {
		Client client1 = new Client(args);
		GUI userGUI1 = new GUI(client1);
		
		
		/*Client client2 = new Client();
		GUI userGUI2 = new GUI(client2);
		
		Client client3 = new Client();
		GUI userGUI3 = new GUI(client3);
		
		
		Client client4 = new Client();
		GUI userGUI4 = new GUI(client4);*/
	}

}
