package mp3magic.data;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;

class ConnectionManager
{
	private Connection conn;
	
	public ConnectionManager() throws SQLException
	{
		try
		{
			Class.forName("org.gjt.mm.mysql.Driver");
		}
		catch (Exception e)
		{
		}
		conn = DriverManager.getConnection("jdbc:mysql://localhost/mp3magic","mp3magic","mp3magic");
	}
	
	private void getLock(String[] tables, String locktype) throws SQLException
	{
		StringBuffer buffer = new StringBuffer("LOCK TABLES ");
		for (int loop=0; loop<tables.length; loop++)
		{
			buffer.append(tables[loop]+" "+locktype);
			if ((loop+1)==tables.length)
			{
				buffer.append(";");
			}
			else
			{
				buffer.append(",");
			}
		}
		executeUpdate(buffer.toString());
	}
	
	public void getReadLock(String[] tables) throws SQLException
	{
		getLock(tables,"READ");
	}
	
	public void getWriteLock(String[] tables) throws SQLException
	{
		getLock(tables,"WRITE");
	}
	
	public void releaseLock() throws SQLException
	{
		executeUpdate("UNLOCK TABLES;");
	}
	
	public ResultSet executeQuery(String query) throws SQLException
	{
		System.out.println(query);
		return conn.createStatement().executeQuery(query);
	}
	
	public void executeUpdate(String update) throws SQLException
	{
		System.out.println(update);
		conn.createStatement().executeUpdate(update);
	}
}
