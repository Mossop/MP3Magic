package mp3magic.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Listener extends Thread {
	
	private static void main(String[] args) {
		//Create the Listener on port 3078
		Listener theProg = new Listener(3078);
	}
	
	//servers is ArrayList of all currently operating Servers
	private ArrayList servers = new ArrayList();
	private int maxUsers;
	private ServerSocket portListener;	
	
	//Constructor for Listener
	public Listener(int port) {
	
		maxUsers = 20;
		
		try {
			portListener = new ServerSocket(port, maxUsers);
			while(true) {
				//Accept new connection on port
				Socket connection = portListener.accept();
				//Create new server with Socket obtained above
				createServer(connection);
			}
		} catch (IOException e) {
			System.out.println("An error has occurred creating a new Server");
			//e.printStackTrace();
			//System.exit(1);
		}
	}
	
	//Create a new server to handle an additional client
	public void createServer(Socket conn) {
		Socket connection = conn;
		servers.add(new Server(connection));
	}

	//Return an ArrayList of all the Users currently connected
	public ArrayList getUser() {
		int serversSize = servers.size();
		ArrayList allUsers = new ArrayList();
		for (int server = 0; server < serversSize; server++) {
			Server currentServer = (Server)servers.get(server);
			allUsers.add(currentServer.getUser());
		}	
		return(allUsers);
	}
}
