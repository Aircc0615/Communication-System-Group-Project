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
		Client client = new Client();
		GUI userGUI = new GUI(client);
	}

}
