package mp3magic.server;
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Integer;
import mp3magic.data.*;
import java.sql.SQLException;

public class Server extends Thread {

	private Socket connection;
	private BufferedReader input;
	private ObjectOutputStream output;
	private LinkedList reply;
	private Integer errorCode;
	private User currentUser;
	protected boolean threadSuspended = true;
	private int CommandSetLength = 24;
	private String CommandSet[] = { 	"login", "logout", "getfavorites", "creatplaylist", "createtrack", 
					"getplaylists", "gettracks", "deleteplaylist", "deletetrack", 
					"findtrack", "findplaylist", "setname", "gettracks", "addtrack", 
					"removetrack", "movetrack", "swaptrack", "copyplaylist", 
					"appendplaylist", "edittrack", "addplaylist", "removeplaylist", 
					"moveplaylist", "swapplaylist" };

	//Constructor for new server thread
	public Server(Socket s) {
		connection = s;
		start();	
	}

	private int ExtractCommand( String cmd ) {
		int tmp = -1;
		for (int i=0; i < CommandSetLength; i++)
			if ( CommandSet[i].equals( cmd ) ) 
				tmp = i;
		return tmp;
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
		//System.out.println(command);
		processRequest(command);
	}
	
	//Initates Server
	public void init() {
	
	}
	
	//Not sure what this does
	public void getUser() {
	
	}
	public void sendReply(LinkedList theResponse, Integer theError) {
		try {
			System.out.println("Should be sending objects");
			//output.writeObject(new LinkedList());
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
		try {
			output.close();
			input.close();
		} catch (IOException e) {
			processException(new Integer(1));
		}
	}

	public void processRequest(String message) {
		// Create message objects.
		LinkedList msgList = new LinkedList();
		int msgError = 0;

		System.out.println("\nProcessing request: " + message);
		StringTokenizer st = new StringTokenizer(message, "@");
		int cmd = ExtractCommand( st.nextToken() ); 
		System.out.println( CommandSet[cmd] ); 
		String p[] = new String[20];
		int pSize = 0;
		while ( st.hasMoreTokens() )
			p[pSize++] = st.nextToken();

		for (int i=0; i < pSize; i++)
			System.out.println(p[i]);

		switch (cmd) {
		case -1: 
			System.out.println("Error"); break;
		case 0: 
		// login
			if ( p[1].equals("t") ) {
				System.out.println("CreateUser " + p[0]);
				try {
					MP3Data msg = MP3Data.getData();
					currentUser = msg.createUser( p[0] );
					msgList.add( currentUser );
					msgError = -69;
					msg = null;
				} catch (SQLException e) {
					System.out.println("Error createUser");
					msgError = 100;
				}
			}
			else {
				System.out.println("Login " + p[0]);
				try {
					MP3Data msg = MP3Data.getData();
					currentUser = msg.getUser( p[0] ) ;
					msgList.add( currentUser );
					msgError = -69;
					msg = null;
				} catch (SQLException e) {
					System.err.println("Error createUser");
					msgError = 101;
				}
			}
			break;
		case 1: 
		// logout
		if (currentUser != null) {
			if (p[0].equals("t")) {
					System.out.println("Deleting User");
					try {
						MP3Data msg = MP3Data.getData();
						msg.deleteUser( currentUser );
						msgError = -69;
						msg = null;
						currentUser = null;
					} catch (SQLException e) {
						System.out.println("Error deleting user");
						msgError = 102;
					}
			}
		}
			shutdownServer();					
			System.out.println("Destroying server");
			break;
		}
	if (cmd != 1) {
		Integer msgErrorInteger = new Integer(msgError);
		sendReply(msgList, msgErrorInteger);
	}
	}

}
