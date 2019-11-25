import java.io.*;

public class Main {

	public static void main(String args[]) {
		System.out.println("Executing...");
		Server s = new Server();
		s.processRequest("login@jon@t@");
		s.processRequest("getfavorites@");
		// s.processRequest("removeplaylist@5@");
	}
}