import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.Iterator; 
import java.lang.Integer;
import java.sql.SQLException;
import java.lang.NullPointerException; 
import mp3magic.data.*;


public class Server {
	private User thisUser; 
	private int CommandSetLength = 24;
	private String CommandSet[] = { 	"login", "logout", "getfavorites", "createplaylist", "createtrack", 
					"getallplaylists", "getalltracks", "deleteplaylist", "deletetrack", 
					"findtrack", "findplaylist", "setname", "gettracks", "addtrack", 
					"removetrack", "movetrack", "swaptrack", "copyplaylist", 
					"appendplaylist", "edittrack", "addplaylist", "removeplaylist", 
					"moveplaylist", "swapplaylist" }; 

	public void Server() {
		System.out.println("Server constructed.");
	}

	private int stoi(String str) {
		Integer i = new Integer(str);
		return i.intValue();
	}

	private int extractCommand( String cmd ) {
		int x = -1;
		for (int i=0; i < CommandSetLength; i++)
			if ( CommandSet[i].equals( cmd ) ) 
				x = i;
		return x;
	}

	public void processRequest(String message) {
		// Create message objects.
		LinkedList msgList = new LinkedList();
		int msgError = -1;

		MP3Data msg = MP3Data.getData();
		// PlayList pl = PlayList();
		Iterator temp;

		System.out.println("\nProcessing request: " + message);
		StringTokenizer st = new StringTokenizer(message, "@");
		int Cmd = extractCommand( st.nextToken() ); 
		System.out.println( CommandSet[Cmd] ); 
		String p[] = new String[20];
		int pSize = 0;
		while ( st.hasMoreTokens() )
			p[pSize++] = st.nextToken();

		for (int i=0; i < pSize; i++)
			System.out.println(p[i]);

		System.out.println("No Of Parameters = " + pSize);

		switch (Cmd) {
		case -1: 
			System.out.println("Error"); break;
		case 0: 
// login
			if ( p[1].equals("t") ) {
				System.out.println("CreateUser ");
				try {
					thisUser = msg.createUser( p[0] );
					msgList.add( thisUser );
					msgError = -69;
				} catch (SQLException e) {
					msgError = 100;
				}
			}
			else {
				System.out.println("Login ");
				try {
					thisUser = msg.getUser( p[0] );
					msgList.add( thisUser );
					msgError = -69;
				} catch (SQLException e) {
					msgError = 101;
				}
			}
			break;
		case 1: 
// logout
			if (p[0].equals("t")) {
				System.out.println("Deleting User");
				try {
					msg.deleteUser( thisUser );
					msgError = -69;
				} catch (SQLException e) {
					msgError = 102;
				}
			}
			// ##### DESTROY SERVER
			System.out.println("Server destoryed");					
			break;
		case 2: 
// getfavorites
			System.out.println( "get favorites" );
			try {
				temp = thisUser.getPlayLists();
				if (temp != null)
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
			} catch (NullPointerException n) {
				msgError = 1081;
			}			
			break;
		case 8: 
// deletetrack
			System.out.println( "delete track" );
			try {
				temp = msg.findTrack( p[0], p[1], null, null, null, 0, 0, 0 );
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
			System.out.println( " find track - KINDA IMPLEMENTED!" ); 
			try {
				for (int j=0; j < 15; j+=2) {
					if (p[j].equals("t") && p[j+1].equals("?"))	p[j+1] = "";
					if (p[j].equals("f"))	p[j+1] = null;
					System.out.println(j);
				}			
				temp = msg.findTrack( p[1], p[3], p[5], p[7], p[9], stoi(p[11]), stoi(p[13]), stoi(p[15]) );
				msgError = -69;
			} catch (SQLException e) {
				msgError = 112;
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
				temp = msg.findPlayList( p[0] ).getTracks();
				while ( temp.hasNext() ) { 
					msgList.add( temp.next() );
				}
				msgError = -69;
			} catch (SQLException e) {
				msgError = 114;
			} catch (NullPointerException n) {
				msgError = 1141;
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
			} catch (NullPointerException n) {
				msgError = 1161;
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
			} catch (NullPointerException n) {
				msgError = 1161;
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
			} catch (NullPointerException n) {
				msgError = 1171;
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
			} catch (NullPointerException n) {
				msgError = 1181;
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
			} catch (NullPointerException n) {
				msgError = 1191;
			}			
			break;			
		case 19:
// edittrack
			System.out.println( "edit track" );
			try {
				temp = msg.findTrack( p[0], p[1], null, null, null, -1, -1, -1 );
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
				System.out.println("HERE");
			} catch (SQLException e) {
				msgError = 121;
			} catch (NullPointerException n) {
				msgError = 1211;
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
			} catch (NullPointerException n) {
				msgError = 1221;
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
			} catch (NullPointerException n) {
				msgError = 1231;
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
			} catch (NullPointerException n) {
				msgError = 1241;
			}			
			break;	
		}
		Integer errorCode = new Integer( msgError );
		// SEND REPLY
		System.out.println("SENDING REPLY");
		msgList = null;
		errorCode = null;
	}

} // end of Server

