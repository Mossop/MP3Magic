package mp3magic.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

public class MP3DataImpl extends MP3Data 
{
	private ConnectionManager conn;
	
	public MP3DataImpl() throws SQLException
	{
		conn = new ConnectionManager();
	}
	
  public Iterator getPlayLists() throws SQLException
  {
		conn.getReadLock(new String[] {"Playlists"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT name FROM Playlists;");
			LinkedList lists = new LinkedList();
			while (results.next())
			{
				lists.add(new PlayListImpl(results.getString(1)));
			}
			conn.releaseLock();
			return lists.iterator();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }
  
  public Iterator getTracks() throws SQLException
  {
		conn.getReadLock(new String[] {"Tracks"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT artist,title,filename,album,genre,filesize,length,bitrate FROM Tracks;");
			LinkedList tracks = new LinkedList();
			while (results.next())
			{
				String artist=results.getString(1);
				String title=results.getString(2);
				String filename=results.getString(3);
				String album=results.getString(4);
				String genre=results.getString(5);
				int filesize=results.getInt(6);
				int length=results.getInt(7);
				int bitrate=results.getInt(8);
				tracks.add(new TrackImpl(artist,title,filename,album,genre,filesize,length,bitrate));
			}
			conn.releaseLock();
			return tracks.iterator();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			conn.releaseLock();
			throw e;
		}
  }
  
  public User getUser(String name) throws SQLException
  {
		conn.getReadLock(new String[] {"Users"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT name FROM Users WHERE name='"+name+"';");
			conn.releaseLock();
			if (results.next())
			{
				return new UserImpl(results.getString(1));
			}
			else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }

  public User createUser(String username) throws SQLException
  {
		conn.getWriteLock(new String[] {"Users"});
		try
		{
			conn.executeUpdate("INSERT INTO Users (name) VALUES ('"+username+"');");
			conn.releaseLock();
			return new UserImpl(username);
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }
  
  public PlayList createPlayList(String playlistname) throws SQLException
  {
		conn.getWriteLock(new String[] {"Playlists"});
		try
		{
			conn.executeUpdate("INSERT INTO Playlists (name) VALUES ('"+playlistname+"');");
			conn.releaseLock();
			return new PlayListImpl(playlistname);
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }
  
  public Track createTrack(String artist, String title) throws SQLException
  {
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("INSERT INTO Tracks (artist,title,filename,album,genre,filesize,length,bitrate) VALUES ('"+artist+"','"+title+"','','','',0,0,0);");
			conn.releaseLock();
			return new TrackImpl(artist,title,"","","",0,0,0);
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }

  public void deleteUser(User user) throws SQLException
  {
		conn.getWriteLock(new String[] {"Users"});
		try
		{
			conn.executeUpdate("DELETE FROM Users WHERE name='"+user.getName()+"';");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }
  
  public void deletePlayList(PlayList playlist) throws SQLException
  {
		conn.getWriteLock(new String[] {"Playlists"});
		try
		{
			conn.executeUpdate("DELETE FROM Playlists WHERE name='"+playlist.getName()+"';");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }
  
  public void deleteTrack(Track track) throws SQLException
  {
		conn.getWriteLock(new String[] {"Tracks"});
		try
		{
			conn.executeUpdate("DELETE FROM Tracks WHERE artist='"+track.getArtist()+"' AND title='"+track.getTitle()+"';");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }

  public Iterator findTrack(String artist, String title, String filename, String album, String genre, int filesize, int length, int bitrate) throws SQLException
  {
  	StringBuffer wherelist = new StringBuffer();
  	if (artist!=null)
  	{
  		wherelist.append("artist LIKE '%"+artist+"%' AND ");
  	}
  	if (title!=null)
  	{
  		wherelist.append("title LIKE '%"+title+"%' AND ");
  	}
  	if (filename!=null)
  	{
  		wherelist.append("filename LIKE '%"+filename+"%' AND ");
  	}
  	if (album!=null)
  	{
  		wherelist.append("album LIKE '%"+album+"%' AND ");
  	}
  	if (genre!=null)
  	{
  		wherelist.append("genre LIKE '%"+genre+"%' AND ");
  	}
  	if (filesize>=0)
  	{
  		wherelist.append("filesize="+filesize+" AND ");
  	}
  	if (length>=0)
  	{
  		wherelist.append("length="+length+" AND ");
  	}
  	if (bitrate>=0)
  	{
  		wherelist.append("bitrate="+bitrate+" AND ");
  	}
  	String qry = " WHERE "+wherelist.toString().substring(0,wherelist.length()-5)+";";
		conn.getReadLock(new String[] {"Tracks"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT artist,title,filename,album,genre,filesize,length,bitrate FROM Tracks"+qry);
			LinkedList tracks = new LinkedList();
			while (results.next())
			{
				artist=results.getString(1);
				title=results.getString(2);
				filename=results.getString(3);
				album=results.getString(4);
				genre=results.getString(5);
				filesize=results.getInt(6);
				length=results.getInt(7);
				bitrate=results.getInt(8);
				tracks.add(new TrackImpl(artist,title,filename,album,genre,filesize,length,bitrate));
			}
			conn.releaseLock();
			return tracks.iterator();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }
  
  public PlayList findPlayList(String playlistname) throws SQLException
  {
		conn.getReadLock(new String[] {"Playlists"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT name FROM Playlists WHERE name='"+playlistname+"';");
			conn.releaseLock();
			if (results.next())
			{
				return new PlayListImpl(results.getString(1));
			}
			else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
  }
	
	public static void main(String[] args)
	{
		try
		{
			MP3Data data = new MP3DataImpl();
			PlayList brit = data.findPlayList("Britney");
			brit.appendPlayList(data.findPlayList("Classic Queen"));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
