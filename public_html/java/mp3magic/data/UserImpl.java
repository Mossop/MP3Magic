package mp3magic.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.sql.ResultSet;
import java.util.LinkedList;

public class UserImpl implements User {
	private String name;
        
	public UserImpl(String name) throws SQLException
	{
		this.name=name;
	}
	
	// data retrieval methods
	public String getName()
	{
		return name;
	}
	
	//data storage methods
	public void setName(String name) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Users","UserPlaylists"});
		try
		{
			conn.executeUpdate("UPDATE Users SET name='"+name+"' WHERE name='"+this.name+"';");
			conn.executeUpdate("UPDATE UserPlaylists SET user='"+name+"' WHERE user='"+this.name+"';");
			this.name=name;
			conn.releaseLock();
		}
    catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	//data manipulation methods
	public int getNumberOfPlayLists() throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getReadLock(new String[] {"UserPlaylists"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT playlist FROM UserPlaylists WHERE user='"+name+"';");
			int count = 0;
			while (results.next())
			{	
				count++;
			}
			conn.releaseLock();
			return count;
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	private int getLength() throws SQLException
	{
		return getNumberOfPlayLists();
	}
	
	public Iterator getPlayLists() throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getReadLock(new String[] {"UserPlaylists"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT playlist FROM UserPlaylists WHERE user='"+name+"';");
			LinkedList playlists = new LinkedList();
			while (results.next())
			{
				String playlist=results.getString(1);
        playlists.add(new PlayListImpl(playlist));
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
	
	public void addPlayList(PlayList playlist) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		int length = getLength();
		conn.getWriteLock(new String[] {"UserPlaylists"});
		try
		{
			conn.executeUpdate("INSERT INTO UserPlaylists (user,position,playlist) VALUES ('"+name+"',"+(length+1)+",'"+playlist.getName()+"');");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}

  public void moveDown(int start, int end, ConnectionManager conn) throws SQLException
  {
		for (int loop=start; loop<=end; loop++)
		{
			conn.executeUpdate("UPDATE UserPlaylists SET position="+(loop-1)+" WHERE user='"+name+"' AND position="+loop+";");
		}
		conn.releaseLock();
	}

	public void moveUp(int start, int end, ConnectionManager conn) throws SQLException
	{	
		for (int loop=end; loop>=start; loop--)
		{
			conn.executeUpdate("UPDATE UserPlaylists SET position="+(loop+1)+" WHERE user='"+name+"' AND position="+loop+";");	
		}
		conn.releaseLock();
	}

	public void addPlayList(PlayList playlist, int position) throws SQLException
	{
		addPlayList(playlist);
		movePlayList(getLength(),position);
	}
	
	public void removePlayList(int position) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		int length=getLength();
		conn.getWriteLock(new String[] {"UserPlaylists"});
		try
		{
			conn.executeUpdate("DELETE FROM UserPlaylists WHERE user='"+name+"' AND position="+position+";");
			moveDown(position+1,length,conn);
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void movePlayList(int oldposition, int newposition) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		int length=getLength();
		conn.getWriteLock(new String[] {"UserPlaylists"});
		try
		{
			conn.executeUpdate("UPDATE UserPlaylists SET position=0 WHERE user='"+name+"' AND position="+oldposition+";");
			moveDown(oldposition+1,length,conn);
			moveUp(newposition,length-1,conn);
			conn.executeUpdate("UPDATE UserPlaylists SET position="+newposition+" WHERE user='"+name+"' AND position=0;");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void swapPlayList(int position1, int position2) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"UserPlaylists"});
		try
		{
			conn.executeUpdate("UPDATE UserPlaylists SET position=0 WHERE user='"+name+"' AND position="+position1+";");
			conn.executeUpdate("UPDATE UserPlaylists SET position="+position1+" WHERE user='"+name+"' AND position="+position2+";");
			conn.executeUpdate("UPDATE UserPlaylists SET position="+position2+" WHERE user='"+name+"' AND position=0;");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
}
