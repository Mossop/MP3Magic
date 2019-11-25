package mp3magic.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.io.Serializable;

public interface Track extends Serializable {
	// data retrieval methods
	public String getArtist();
	public String getTitle();
	public String getFilename();
	public String getAlbum();
	public String getGenre();
	public int getFileSize();
	public int getLength();
	public int getBitRate();
	
	//data storage methods
	public void setArtist(String artist) throws SQLException;
	public void setTitle(String title) throws SQLException;
	public void setFilename(String filename) throws SQLException;
	public void setAlbum(String album) throws SQLException;
	public void setGenre(String genre) throws SQLException;
	public void setFileSize(int filesize) throws SQLException;
	public void setLength(int length) throws SQLException;
	public void setBitRate(int bitrate) throws SQLException;
	
	//data manipulation methods
	public Iterator inPlayLists() throws SQLException;
}
