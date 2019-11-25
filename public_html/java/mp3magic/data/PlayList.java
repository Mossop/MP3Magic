package mp3magic.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.io.Serializable;

public interface PlayList extends Serializable {
	// data retrieval methods
	public String getName();
	
	//data storage methods
	public void setName(String name) throws SQLException;
	
	//data manipulation methods
	public int getLength() throws SQLException;
	public Iterator getTracks() throws SQLException;
	public void addTrack(Track track) throws SQLException;
	public void addTrack(Track track, int position) throws SQLException;
	public void removeTrack(int position) throws SQLException;
	public void moveTrack(int oldposition, int newposition) throws SQLException;
	public void swapTrack(int position1, int position2) throws SQLException;
	public PlayList copyPlayList(String playlistname) throws SQLException;
	public void appendPlayList(PlayList playlist) throws SQLException;
	public Iterator inUsers() throws SQLException;
}
