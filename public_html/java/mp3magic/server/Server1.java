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
	private User thisUser;
	protected boolean threadSuspended = true;
	private int CommandSetLength = 24;
	private String CommandSet[] = { "login", "logout", "getfavorites", "createplaylist", "createtrack", 
					"getallplaylists", "getalltracks", "deleteplaylist", "deletetrack", 
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

	private int stoi(String str) {
			Integer i = new Integer(str);
			return i.intValue();
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
	
	//Returns the user currently logged in 
	public User getUser() {
		return(thisUser);
	}
	public void sendReply(LinkedList theResponse, Integer theError) {
		try {
			System.out.println("Returning objects to Client");
			output.writeObject(theResponse);	//Writing LinkedList of returned objects
			output.writeObject(theError);			//Return Integer errorcode
		} catch (IOException e) {
			processException(new Integer(1)); //An exception has occurred, call handler
		}
	}


	//Returns empty list and error code relating to exception
	public void processException(Integer errorCode) {
		try {
			output.writeObject(new LinkedList());	//Write empty list as client is expecting a List
			output.writeObject(errorCode);				//Write relevant Errocode
		} catch (IOException e) {
			//The applet has quit without logging out if this code is reached
			System.out.println("That'll be the boy! You applet monkey spunker!");
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

	//Jon's bloddy great big parser bit
	public void processRequest(String message) {
		// Create message objects.
		LinkedList msgList = new LinkedList();
		int msgError = -1;
		
		MP3Data msg = MP3Data.getData();
		Iterator temp;

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
					//MP3Data msg = MP3Data.getData();
					thisUser = msg.createUser( p[0] );
					msgList.add( thisUser );
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
					//MP3Data msg = MP3Data.getData();
					thisUser = msg.getUser( p[0] ) ;
					msgList.add( thisUser );
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
		if (thisUser != null) {
			if (p[0].equals("t")) {
					System.out.println("Deleting User");
					try {
						//MP3Data msg = MP3Data.getData();
						msg.deleteUser( thisUser );
						msgError = -69;
						msg = null;
						thisUser = null;
					} catch (SQLException e) {
						System.out.println("Error deleting user");
						msgError = 102;
					}
			}
		}
			shutdownServer();					
			System.out.println("Destroying server");
			break;

		case 2: 
// getfavorites
			System.out.println( "Returns favorites" );
			try {
				temp = thisUser.getPlayLists();
				while (temp.hasNext()) 
					msgList.add( (PlayList) temp.next() );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 103;
			}
			break;
		case 3: 
// createplaylist
			System.out.println( "Creating PlayList" );
			try {
				msgList.add( msg.createPlayList( p[0] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 104;
			}			
			break;
		case 4: 
// createtrack
			System.out.println("Create Track");
			try {
				msgList.add( msg.createTrack( p[0], p[1] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 105;
			}			
			break;
		case 5: 
// getallplaylists
			System.out.println( "Returns all PlayLists" );
			try {
				temp = msg.getPlayLists();
				while (temp.hasNext()) 
					msgList.add( (PlayList) temp.next() );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 106;
			}
			break;
		case 6: 
// getalltracks
			System.out.println( "Returns all Tracks" );
			try {
				temp = msg.getTracks();
				while (temp.hasNext()) 
					msgList.add( (Track) temp.next() );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 106;
			}
			break;
		case 7: 
// deleteplaylist
			System.out.println( "delete playlist" );
			try {
				msg.deletePlayList( msg.findPlayList( p[0] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 108;
			}			
			break;
		case 8: 
// deletetrack
			System.out.println( "delete track" );
			try {
				temp = msg.findTrack( p[0], p[1], null, null, null, -21, -21, -21 );
				if ( temp.hasNext() ) {
					msg.deleteTrack( (Track) temp.next() );
					msgError = -69;  
				}
				else 
					msgError = 108;
			} catch (SQLException e) {
				msgError = 109;
			}			
			break;
		case 9: 
// findtrack
			System.out.println( " find track - NEW!" );
			try {
				for (int j=0; j < 15; j+=2) {
					if (p[j].equals("t") && p[j+1].equals("?"))	p[j+1] = "";
					if (p[j].equals("f"))	p[j+1] = null;
					System.out.println(j);
				}			
				temp = msg.findTrack( p[1], p[3], p[5], p[7], p[9], stoi(p[11]), stoi(p[13]), stoi(p[15]) );
				msgError = -69;

			} catch (SQLException e) {
				msgError = 111;
			}			
			break;
		case 10: 
// findplaylist
			System.out.println( "find playlist" );
			try {
				msgList.add( msg.findPlayList( p[0] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 112;
			}			
			break;			
		case 11: 
// setname
			System.out.println( "set name" );
			try {
				msg.findPlayList( p[0] ).setName( p[1] );
				msgList.add( msg.findPlayList( p[1] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 113;
			}			
			break;			
		case 12: 
// gettracks
			System.out.println( "get tracks" );
			try {
				msgList.add( msg.findPlayList( p[0] ).getTracks() );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 114;
			}			
			break;			
		case 13: 
// addtrack
			System.out.println( "add track" );
			try {
				temp = msg.findTrack( p[1], p[2], null, null, null, -1, -1, -1 );
				if ( temp.hasNext() ) { 
					if (p[3]==null)
						msg.findPlayList( p[0] ).addTrack( (Track) temp.next() );
					else
						msg.findPlayList( p[0] ).addTrack( (Track) temp.next(), stoi(p[3]) );
					msgList.add( msg.findPlayList( p[0] ) );
					msgError = -69;  
				}
				else 
					msgError = 125;
				msgError = -69;
			} catch (SQLException e) {
				msgError = 126;
			}			
			break;			
		case 14: 
// removetrack
			System.out.println( "remove track" );
			try {
				msg.findPlayList( p[0] ).removeTrack( stoi(p[1]) );
				msgList.add( msg.findPlayList( p[0] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 115;
			}			
			break;			
		case 15: 
// movetrack
			System.out.println( "move track" );
			try {
				msg.findPlayList( p[0] ).moveTrack( stoi(p[1]), stoi(p[2]) );
				msgList.add( msg.findPlayList( p[0] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 116;
			}			
			break;			
		case 16: 
// swaptrack
			System.out.println( "swap track" );
			try {
				msg.findPlayList( p[0] ).swapTrack( stoi(p[1]), stoi(p[2]) );
				msgList.add( msg.findPlayList( p[0] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 117;
			}			
			break;			
		case 17: 
// copyplaylist
			System.out.println( "copy playlist" );
			try {
				msgList.add( msg.findPlayList( p[0] ).copyPlayList( p[1] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 118;
			}			
			break;			
		case 18: 
// appendplaylist
			System.out.println( "append playlist" );
			try {
				msg.findPlayList( p[0] ).appendPlayList( msg.findPlayList( p[1] ) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 119;
			}			
			break;			
		case 19:
// edittrack
			System.out.println( "edit track" );
			try {
				temp = msg.findTrack( p[0], p[1], null, null, null, -21, -21, -21 );
				if ( temp.hasNext() ) { 
					Track t = (Track) temp.next();					
					t.setArtist( p[2] );
					t.setTitle( p[3] );
					t.setFilename( p[4] );
					t.setAlbum( p[5] );
					t.setGenre( p[6] );
					t.setFileSize( stoi(p[7]) );
					t.setLength( stoi(p[8]) );
					t.setBitRate( stoi(p[9]) );
					msgList.add( t );
					t = null;
					msgError = -69; 
				}
				else 
					msgError = 125;

			} catch (SQLException e) {
				msgError = 120;
			}			
			break;			
		case 20:
// addplaylist
			System.out.println( "add playlist" );
			try {
				thisUser.addPlayList( msg.findPlayList( p[0] ), stoi(p[1]) );
				temp = thisUser.getPlayLists();
				while (temp.hasNext()) 
					msgList.add( (PlayList) temp.next() );				
				msgError = -69;
			} catch (SQLException e) {
				msgError = 121;
			}			
			break;			
		case 21:
// removeplaylist
			System.out.println( "remove playlist" );
			try {
				thisUser.removePlayList( stoi(p[0]) );
				temp = thisUser.getPlayLists();
				while (temp.hasNext()) 
					msgList.add( (PlayList) temp.next() );				
				msgError = -69;
			} catch (SQLException e) {
				msgError = 122;
			}			
			break;	
		case 22:
// moveplaylist
			System.out.println( "move playlist" );
			try {
				thisUser.movePlayList( stoi(p[0]), stoi(p[1]) );
				temp = thisUser.getPlayLists();
				while (temp.hasNext()) 
					msgList.add( (PlayList) temp.next() );				
				msgError = -69;
			} catch (SQLException e) {
				msgError = 123;
			}			
			break;	
		case 23:
// swapplaylist
			System.out.println( "swap playlist" );
			try {
				thisUser.swapPlayList( stoi(p[0]), stoi(p[1]) );
				temp = thisUser.getPlayLists();
				while (temp.hasNext()) 
					msgList.add( (PlayList) temp.next() );				
				msgError = -69;
			} catch (SQLException e) {
				msgError = 124;
			}			
			break;	
		}
	//Only send return objects if the command was not logout as connections will be already
	//closed if logout command has been recieved 
	if (cmd != 1) {
		System.out.println("This is why");
		Integer msgErrorInteger = new Integer(msgError);
		sendReply(msgList, msgErrorInteger);
	}
	msgList = null;
	msgError = 0;
	}

}



