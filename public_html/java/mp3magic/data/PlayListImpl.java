package mp3magic.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.sql.ResultSet;
import java.util.LinkedList;

public class PlayListImpl implements PlayList
{
	private String name;
		
	public PlayListImpl(String name) throws SQLException
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
		conn.getWriteLock(new String[] {"Playlists","UserPlaylists","PlaylistTracks"});
		try
		{
			conn.executeUpdate("UPDATE Playlists SET name='"+name+"' WHERE name='"+this.name+"';");
			conn.executeUpdate("UPDATE UserPlaylists SET playlist='"+name+"' WHERE playlist='"+this.name+"';");
			conn.executeUpdate("UPDATE PlaylistTracks SET playlist='"+name+"' WHERE playlist='"+this.name+"';");
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
	public int getLength() throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getReadLock(new String[] {"PlaylistTracks"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT trackartist FROM PlaylistTracks WHERE playlist='"+name+"';");
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
	
	public Iterator getTracks() throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getReadLock(new String[] {"PlaylistTracks","Tracks"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT artist,title,filename,album,genre,filesize,length,bitrate FROM PlaylistTracks,Tracks WHERE playlist='"+name+"' AND trackartist=artist and tracktitle=title;");
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
			conn.releaseLock();
			throw e;
		}
	}
	
	public void addTrack(Track track) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		int length = getLength();
		conn.getWriteLock(new String[] {"PlaylistTracks"});
		try
		{
			conn.executeUpdate("INSERT INTO PlaylistTracks (playlist,position,trackartist,tracktitle) VALUES ('"+name+"',"+(length+1)+",'"+track.getArtist()+"','"+track.getTitle()+"');");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	private void moveDown(int start, int end, ConnectionManager conn) throws SQLException
	{
		for (int loop=start; loop<=end; loop++)
		{
			conn.executeUpdate("UPDATE PlaylistTracks SET position="+(loop-1)+" WHERE playlist='"+name+"' AND position="+loop+";");
		}
		conn.releaseLock();
	}
	
	private void moveUp(int start, int end, ConnectionManager conn) throws SQLException
	{
		for (int loop=end; loop>=start; loop--)
		{
			conn.executeUpdate("UPDATE PlaylistTracks SET position="+(loop+1)+" WHERE playlist='"+name+"' AND position="+loop+";");
		}
		conn.releaseLock();
	}
	
	public void addTrack(Track track, int position) throws SQLException
	{
		addTrack(track);
		moveTrack(getLength(),position);
	}
	
	public void removeTrack(int position) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		int length=getLength();
		conn.getWriteLock(new String[] {"PlaylistTracks"});
		try
		{
			conn.executeUpdate("DELETE FROM PlaylistTracks WHERE playlist='"+name+"' AND position="+position+";");
			moveDown(position+1,length,conn);
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void moveTrack(int oldposition, int newposition) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		int length=getLength();
		conn.getWriteLock(new String[] {"PlaylistTracks"});
		try
		{
			conn.executeUpdate("UPDATE PlaylistTracks SET position=0 WHERE playlist='"+name+"' AND position="+oldposition+";");
			moveDown(oldposition+1,length,conn);
			moveUp(newposition,length-1,conn);
			conn.executeUpdate("UPDATE PlaylistTracks SET position="+newposition+" WHERE playlist='"+name+"' AND position=0;");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void swapTrack(int position1, int position2) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"PlaylistTracks"});
		try
		{
			conn.executeUpdate("UPDATE PlaylistTracks SET position=0 WHERE playlist='"+name+"' AND position="+position1+";");
			conn.executeUpdate("UPDATE PlaylistTracks SET position="+position1+" WHERE playlist='"+name+"' AND position="+position2+";");
			conn.executeUpdate("UPDATE PlaylistTracks SET position="+position2+" WHERE playlist='"+name+"' AND position=0;");
			conn.releaseLock();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public PlayList copyPlayList(String playlistname) throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getWriteLock(new String[] {"Playlists"});
		try
		{
			PlayList result = new PlayListImpl(playlistname);
			conn.executeUpdate("INSERT INTO Playlists (name) VALUES ('"+playlistname+"');");
			conn.releaseLock();
			result.appendPlayList(this);
			return result;
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
	
	public void appendPlayList(PlayList playlist) throws SQLException
	{
		Iterator tracks = playlist.getTracks();
		while (tracks.hasNext())
		{
			addTrack((Track)tracks.next());
		}
	}
	
	public Iterator inUsers() throws SQLException
	{
		ConnectionManager conn = new ConnectionManager();
		conn.getReadLock(new String[] {"UserPlaylists"});
		try
		{
			ResultSet results = conn.executeQuery("SELECT user FROM UserPlaylists WHERE playlist='"+name+"';");
			LinkedList users = new LinkedList();
			while (results.next())
			{
				users.add(new UserImpl(results.getString(1)));
			}
			conn.releaseLock();
			return users.iterator();
		}
		catch (SQLException e)
		{
			conn.releaseLock();
			throw e;
		}
	}
}
