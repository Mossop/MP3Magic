package mp3magic.server;
import java.net.*;
import java.io.*;
import java.util.*;
import mp3magic.data.*;

public class Server extends Thread {

	private Socket connection;
	private BufferedReader input;
	private ObjectOutputStream output;
	private LinkedList reply;
	private Integer errorCode;
	private User currentUser;
	protected boolean threadSuspended = true;

	//Constructor for new server thread
	public Server(Socket s) {
		connection = s;
		start();	
	}

	public void run() {		
		//A debug statement
		System.out.println("A New server has been created");
		try {
			input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			output = new ObjectOutputStream(connection.getOutputStream());
			//System.out.println("Got passed creating streams");
			//System.out.println(input.readLine());
			while(true) {
				String command = input.readLine();
				if (command == null) {
					System.out.println("This server is closed");	
					break;
				}
				parseCommand(command);
			}
		} catch (IOException e) {
			processException(new Integer(1));
		}
	}

	//Takes string, formatted as in CommandSet and parses it
	public void parseCommand(String command) {
		System.out.println(command);
	}

	//Returns object(s) resulting from the command initiated in response to the formatted string in parseCommand()	public void sendResponse() {		//ObjectOutputStream.writeObject("SOME TEXT IN A String OBJECT");	}		//Initates Server	public void init() {		}		//Not sure what this does	public void getUser() {		}
	public void sendReply(LinkedList theResponse, Integer theError) {
		try {
			output.writeObject(theResponse);
			output.writeObject(theError);
		} catch (IOException e) {
			processException(new Integer(1));
		}
	}


	//Returns empty list and error code relating to exception
	public void processException(Integer errorCode) {
		try {
			output.writeObject(new LinkedList());
			output.writeObject(errorCode);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	//Closes the input and output streams to shutdown the server
	public void shutdownServer() {
		output.close();
		input.close();
	}
}
