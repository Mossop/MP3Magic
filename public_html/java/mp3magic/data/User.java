package mp3magic.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.io.Serializable;

public interface User extends Serializable {
	// data retrieval methods
	public String getName();
	
	//data storage methods
	public void setName(String name) throws SQLException;
	
	//data manipulation methods
	public int getNumberOfPlayLists() throws SQLException;
	public Iterator getPlayLists() throws SQLException;
	public void addPlayList(PlayList playlist) throws SQLException;
	public void addPlayList(PlayList playlist, int position) throws SQLException;
	public void removePlayList(int position) throws SQLException;
	public void movePlayList(int oldposition, int newposition) throws SQLException;
	public void swapPlayList(int position1, int position2) throws SQLException;
}
