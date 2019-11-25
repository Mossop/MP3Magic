package mp3magic.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.sql.ResultSet;
import java.util.LinkedList;

public class TrackImpl implements Track
{
	private String artist;
	private String title;
	private String filename;
	private String album;
	private String genre;
	private int filesize;
	private int length;
	private int bitrate;

	public TrackImpl(String artist, String title, String filename, String album, String genre, int filesize, int length, int bitrate) throws SQLException
	{
		this.artist=artist;
		this.title=title;
		this.filename=filename;
		this.album=album;
		this.genre=genre;
		this.filesize=filesize;
		this.length=length;
		this.bitrate=bitrate;
	}
	
	// data storage methods
	public void setArtist(String artist) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks","PlaylistTracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET artist='"+artist+"' WHERE artist='"+this.artist+"' AND title='"+title+"';");
			conn.executeUpdate("UPDATE PlaylistTracks SET trackartist='"+artist+"' WHERE trackartist='"+this.artist+"' AND tracktitle='"+title+"';");
			this.artist=artist;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void setTitle(String title) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks","PlaylistTracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET title='"+title+"' WHERE artist='"+this.artist+"' AND title='"+this.title+"';");
			conn.executeUpdate("UPDATE PlaylistTracks SET trackartist='"+artist+"' WHERE tracktitle='"+this.title+"' AND trackartist='"+this.artist+"';");
			this.title=title;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}	
	}
	
	public void setFilename(String filename) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET filename='"+filename+"' WHERE filename='"+this.filename+"';");
			this.filename=filename;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void setAlbum(String album) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET album='"+album+"' WHERE album='"+this.album+"';");
			this.album=album;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void setGenre(String genre) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET genre='"+genre+"' WHERE genre='"+this.genre+"';");
			this.genre=genre;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void setFileSize(int filesize) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET filesize='"+filesize+"' WHERE filesize='"+this.filesize+"';");
			this.filesize=filesize;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void setLength(int length) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET length='"+length+"' WHERE length='"+this.length+"';");
			this.length=length;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void setBitRate(int bitrate) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("UPDATE Tracks SET bitrate='"+bitrate+"' WHERE bitrate='"+this.bitrate+"';");
			this.bitrate=length;
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	//data retrieval methods
	public String getArtist()
	{
		return artist;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getFilename() 
	{
		return filename;
	}
	
	public String getAlbum()
	{
		return album;
	}
	
	public String getGenre()
	{
		return genre;
	}
	
	public int getFileSize()
	{
		return filesize;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public int getBitRate()
	{
		return bitrate;
	}
	
	//data manipulation methods
	public Iterator inPlayLists() throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getReadLock(new String[] {"PlaylistTracks"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT track FROM PlaylistTracks WHERE title='"+title+"';");		
			LinkedList playlists = new LinkedList();
			while (results.next())
			{
				playlists.add(new TrackImpl(results.getString(1),results.getString(2),results.getString(3),results.getString(4),results.getString(5),results.getInt(6),results.getInt(7),results.getInt(8)));
			}	 
			conn.releaseLock();
			return playlists.iterator();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
}
