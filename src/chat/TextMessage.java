package chat;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TextMessage implements Serializable{
	private String text;
	private String username;
	private int userId;
	private Instant timestamp;

	//Constructor for existing textmessages
	public TextMessage(String text, String username, int userId, Instant timestamp) {
		this.text = text;
		this.username = username;
		this.userId = userId;
		this.timestamp = timestamp;
	}
	

	//Constructor for new textmessages
	public TextMessage(String text, String username, int userId) {
		this.text = text;
		this.username = username;
		this.userId = userId;
		this.timestamp = Instant.now();
	}

	//getters
	public String getText() {
		return text;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public Instant getTimestamp() {
		return timestamp;
	}


	public String getTimeSent() {
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a")
	            .withZone(ZoneId.systemDefault());

	    return formatter.format(timestamp);
	}
}	