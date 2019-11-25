package mp3magic.data;
import java.sql.SQLException;
import java.util.Iterator;

public abstract class MP3Data {
        public static MP3Data getData()
        {
        	try
        	{
        		return new MP3DataImpl();
        	}
        	catch (SQLException e)
        	{
        		return null;
        	}
        }

        public abstract Iterator getPlayLists() throws SQLException;
        public abstract Iterator getTracks() throws SQLException;
        public abstract User getUser(String name) throws SQLException;

        public abstract User createUser(String username) throws SQLException;
        public abstract PlayList createPlayList(String playlistname) throws SQLException;
        public abstract Track createTrack(String artist, String title) throws SQLException;

        public abstract void deleteUser(User user) throws SQLException;
        public abstract void deletePlayList(PlayList playlist) throws SQLException;
        public abstract void deleteTrack(Track track) throws SQLException;

        public abstract Iterator findTrack(String artist, String title, String filename, String album, String genre, int filesize, int length, int bitrate) throws SQLException;
        public abstract PlayList findPlayList(String playlistname) throws SQLException;
}
