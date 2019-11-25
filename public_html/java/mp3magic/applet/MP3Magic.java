package mp3magic.applet;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;
import mp3magic.data.User;
import mp3magic.data.Track;
import mp3magic.data.PlayList;

// Connection Mng sets up a communication pipe with
// the MP3Magic server and controls the flow.
// Also holds 'reply', which acts as an information
// store for the Applet.

class ConnectionMng extends MP3Magic {
  // These variables can be accessed by anyone to check status, etc..
  static LinkedList reply = new LinkedList();
  static java.lang.Integer errorCode;
  static int status;
  final static int NOT_CONNECTED = 0;
  final static int CANT_CONNECT = 1;
  final static int CLASS_NOT_FOUND = 2;
  final static int DISCONNECTED = 3;
  final static int CONNECTED = 4;

  // Local variables used to hold the pipe, etc..
  private static PrintWriter out;
  private static ObjectInputStream in;
  private static Socket sock = null;

  // CONSTRUCTOR - initialises the variables
  public ConnectionMng() {
    errorCode = new java.lang.Integer(0);
    status = NOT_CONNECTED;
  }

  // Sent by MP3Magic to connect to the server.
  // Host name and port are HARD CODED because of
  // Applet restrictions, etc..
  public int connect() {
    try {
      sock = new Socket("eeguinness.swan.ac.uk", 3078);
      out = new PrintWriter(sock.getOutputStream(), true);
      in = new ObjectInputStream(sock.getInputStream());
    }
    catch (IOException e) {
      status = CANT_CONNECT;
      return CANT_CONNECT;
    }
    status = CONNECTED;
    return CONNECTED;
  }

  // Send the server a command. This can be called by
  // Any member of MP3Magic. This also calls getReply so
  // the caller doesn't have to.
  public int sendCommand(String s) {
    if (status != CONNECTED) return status;
    out.println(s);
    return getReply();
  }

  // Private! Gets the reply (LinkedList) and errorCode from
  // the server. This is then publically accessable.
  private int getReply() {
    try {
      reply = (LinkedList)in.readObject();
      errorCode = (java.lang.Integer)in.readObject();
    } catch (IOException e) {
      status = DISCONNECTED;
      return DISCONNECTED;
    } catch (ClassNotFoundException e) {
      status = CLASS_NOT_FOUND;
      return CLASS_NOT_FOUND;
    }
    if (errorCode.intValue() > 0) {
      if (errorCode.equals(new Integer(1082)))
        JOptionPane.showMessageDialog(null, "Playlist in use by others!", "Oops!", JOptionPane.ERROR_MESSAGE);
    }
    if (reply.size() > 0) {
      if (reply.get(0) == null) reply.clear();
    }
    return CONNECTED;
  }

  // Shutdown the connections and set the status to DISCONNECTED
  public void shutdown() {
    try {
      in.close();
      out.close();
      sock.close();
      status = DISCONNECTED;
    } catch (IOException e) {} // Ignore as it's shutdown :o)
  }
}


// The DataFrame is the main GIU screen. All panes (windows) are
// part of the DataFrame. Declare this incase there are some
// global methods we can use for them (setButton!)
class DataFrame extends MP3Magic {
  // Do nothing as this is only for the polymorph.
  void doAction(String cmd) {}
  void setButton(JButton b, String s, ActionListener al) {
    b.setActionCommand(s);
    b.addActionListener(al);
  }
}


// This is the Session login pane.
class SessionLogin extends DataFrame {
  JTextField username = new JTextField(10);
  JButton login = new JButton("Log in");
  JButton clear = new JButton("Clear");
  JCheckBox newuser = new JCheckBox("I'm a New User!");

  Box top = Box.createHorizontalBox();
  Box bottom = Box.createHorizontalBox();
  Box errbox = Box.createHorizontalBox();
  Box frame = Box.createVerticalBox();
  JLabel error = new JLabel("");

  // CONSTRUCTOR - Builds the pane and adds actionListeners.
  public SessionLogin(Container cp) {
    cp.removeAll();
    cp.setLayout(new FlowLayout(FlowLayout.LEFT));

    top.add(Box.createHorizontalStrut(180));
    bottom.add(Box.createHorizontalStrut(180));
    errbox.add(Box.createHorizontalStrut(180));

    top.add(username);
    setButton(login, "SessionLogin.login", al);
    top.add(login);
    setButton(clear, "SessionLogin.clear", al);
    top.add(clear);
    bottom.add(newuser);
    errbox.add(error);
    frame.add(top);
    frame.add(bottom);
    frame.add(error);
    cp.add(frame);
  }

  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {
    // If clear is pressed, simply clear the username text box.
    if (cmd.equals("SessionLogin.clear")) username.setText("");

    // If the login key is pressed, send messages to the server...
    else if (cmd.equals("SessionLogin.login")) {
      // Build command to send to server
      String c = new String("login@" + username.getText() + "@");
      if (newuser.isSelected()) c += "t@";
      else c += "f@";

      // Send it and check the server is still there.
      if (server.sendCommand(c) != server.CONNECTED) {
        // If not, shutdown and show disconnected pane
        pane = new SessionDisconnect(getContentPane());
        server.shutdown();
      }

      // If it is still there...
      else {
        // Check there is a user in the reply. If so, I'm there!
        // If not, print error message
        if (server.reply.size() != 1) error.setText("User '" + username.getText() + "' Unknown.");
        else {
          // I'm there! Now get my favorites list. I don't need to
          // check to see if the server is still there, as the MP3Magic
          // superclass will do this after I've painted the new pane.
          me = (User)server.reply.get(0);
          server.sendCommand("getfavorites@");
          pane = new PlaylistList(cp);
        }
      }
    }
  }
}


// Very important. Logout pane. Shown if the user selects
// Logout from the session menu.
class SessionLogout extends DataFrame {
  JButton logout = new JButton("Log out");
  JButton cancel = new JButton("Cancel");
  JCheckBox deluser = new JCheckBox("Delete me");
  JPanel top = new JPanel();
  JPanel bottom = new JPanel();

  // CONSTRUCTOR - Build the window, add the actionListeners
  public SessionLogout(Container cp) {
    cp.removeAll();
    cp.setLayout(new FlowLayout());
    top.setPreferredSize(new Dimension(600,35));
    bottom.setPreferredSize(new Dimension(600,35));
    setButton(logout, "SessionLogout.Logout", al);
    top.add(logout);
    setButton(cancel, "SessionLogout.Cancel", al);
    top.add(cancel);
    bottom.add(deluser);
    cp.add(top);
    cp.add(bottom);
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above) 
  void doAction(String cmd) {
    // The user wants to stay
    if (cmd.equals("SessionLogout.Cancel")) {

      //Get the favorites and push them back to that pane.
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }
    // Oh dear - the user *really* wants to log out.
    else if (cmd.equals("SessionLogout.Logout")) {
      me = null;	// Just to tell everyone I'm not logged in

      // Send the server the relavent command (if they want to be deleted or not)
      if (deluser.isSelected()) server.sendCommand("logout@t@");
      else server.sendCommand("logout@f@");

      // Don't bother with anything else... just kill everything
      server.shutdown();
      pane = new SessionDisconnect(cp);
    }
  }
}


// This is the MP3 Search pane. Lots of controls, so...
class MP3Search extends DataFrame {
  JLabel lblArtist = new JLabel("Artist:");
  JTextField artist = new JTextField(20);
  JCheckBox chkArtist = new JCheckBox("Include in Search");
  JLabel lblTitle = new JLabel("Title:");
  JTextField title = new JTextField(20);
  JCheckBox chkTitle = new JCheckBox("Include in Search");
  JLabel lblFilename = new JLabel("Filename:");
  JTextField filename = new JTextField(20);
  JCheckBox chkFilename = new JCheckBox("Include in Search");
  JLabel lblAlbum = new JLabel("Album:");
  JTextField album = new JTextField(20);
  JCheckBox chkAlbum = new JCheckBox("Include in Search");
  JLabel lblGenre = new JLabel("Genre:");
  JTextField genre = new JTextField(20);
  JCheckBox chkGenre = new JCheckBox("Include in Search");
  JLabel lblSize = new JLabel("Size:");
  JTextField size = new JTextField(20);
  JCheckBox chkSize = new JCheckBox("Include in Search");
  JLabel lblLength = new JLabel("Length:");
  JTextField length = new JTextField(20);
  JCheckBox chkLength = new JCheckBox("Include in Search");
  JLabel lblBitrate = new JLabel("Bitrate:");
  JTextField bitrate = new JTextField(20);
  JCheckBox chkBitrate = new JCheckBox("Include in Search");

  Box[] pnlList = new Box[8];
  Box frame = Box.createVerticalBox();

  JButton search = new JButton("Search");

  // CONSTRUCTOR - Builds the pane and adds actionListener
  public MP3Search(Container cp) {
    cp.removeAll();
    cp.setLayout(new FlowLayout(FlowLayout.LEFT));
    for (int i=0; i<8; i++) {
      pnlList[i] = Box.createHorizontalBox();
      pnlList[i].add(Box.createHorizontalStrut(75));
    }
    pnlList[0].add(lblArtist);
    pnlList[0].add(Box.createHorizontalStrut(10));
    pnlList[0].add(artist);
    pnlList[0].add(Box.createHorizontalStrut(10));
    pnlList[0].add(chkArtist);
    pnlList[1].add(lblTitle);
    pnlList[1].add(Box.createHorizontalStrut(10));
    pnlList[1].add(title);
    pnlList[1].add(Box.createHorizontalStrut(10));
    pnlList[1].add(chkTitle);
    pnlList[2].add(lblFilename);
    pnlList[2].add(Box.createHorizontalStrut(10));
    pnlList[2].add(filename);
    pnlList[2].add(Box.createHorizontalStrut(10));
    pnlList[2].add(chkFilename);
    pnlList[3].add(lblAlbum);
    pnlList[3].add(Box.createHorizontalStrut(10));
    pnlList[3].add(album);
    pnlList[3].add(Box.createHorizontalStrut(10));
    pnlList[3].add(chkAlbum);
    pnlList[4].add(lblGenre);
    pnlList[4].add(Box.createHorizontalStrut(10));
    pnlList[4].add(genre);
    pnlList[4].add(Box.createHorizontalStrut(10));
    pnlList[4].add(chkGenre);
    pnlList[5].add(lblSize);
    pnlList[5].add(Box.createHorizontalStrut(10));
    pnlList[5].add(size);
    pnlList[5].add(Box.createHorizontalStrut(10));
    pnlList[5].add(chkSize);
    pnlList[6].add(lblLength);
    pnlList[6].add(Box.createHorizontalStrut(10));
    pnlList[6].add(length);
    pnlList[6].add(Box.createHorizontalStrut(10));
    pnlList[6].add(chkLength);
    pnlList[7].add(lblBitrate);
    pnlList[7].add(Box.createHorizontalStrut(10));
    pnlList[7].add(bitrate);
    pnlList[7].add(Box.createHorizontalStrut(10));
    pnlList[7].add(chkBitrate);
    for (int i=0; i<8; i++) frame.add(pnlList[i]);
    setButton(search, "MP3Search.Search", al);
    frame.add(search);
    cp.add(frame);
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {

    // Only one command here. Search button.
    if (cmd.equals("MP3Search.Search")) {

      // Empty string can't be sent to the server, so send a single '?'
      // Server will then change all single '?' to ""
      if (artist.getText().equals("")) artist.setText("?");
      if (title.getText().equals("")) title.setText("?");
      if (filename.getText().equals("")) filename.setText("?");
      if (album.getText().equals("")) album.setText("?");
      if (genre.getText().equals("")) genre.setText("?");
      if (size.getText().equals("")) size.setText("-1");
      if (length.getText().equals("")) length.setText("-1");
      if (bitrate.getText().equals("")) bitrate.setText("-1");
      try {
        int ip = (new Integer(size.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Filesize!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      try {
        int ip = (new Integer(length.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Length!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      try {
        int ip = (new Integer(bitrate.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Bit Rate!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Build command to send to server. Messy, but it works.
      String c = new String("findtrack@");
      if (chkArtist.isSelected()) c+= "t@" + artist.getText() + "@";
      else c+= "f@?@";
      if (chkTitle.isSelected()) c+= "t@" + title.getText() + "@";
      else c+= "f@?@";
      if (chkFilename.isSelected()) c+= "t@" + filename.getText() + "@";
      else c+= "f@?@";
      if (chkAlbum.isSelected()) c+= "t@" + album.getText() + "@";
      else c+= "f@?@";
      if (chkGenre.isSelected()) c+= "t@" + genre.getText() + "@";
      else c+= "f@?@";
      if (chkSize.isSelected()) c+= "t@" + size.getText() + "@";
      else c+= "f@-1@";
      if (chkLength.isSelected()) c+= "t@" + length.getText() + "@";
      else c+= "f@-1@";
      if (chkBitrate.isSelected()) c+= "t@" + bitrate.getText() + "@";
      else c+= "f@-1@";

      // Send the command an load the MP3List Pane
      server.sendCommand(c);
      pane = new MP3List(cp);
    }
  }
}


// Search for a Playlist. Very small window.
class PlaylistSearch extends DataFrame {
  JLabel lbl_name = new JLabel("Playlist Name:");
  JTextField playlist = new JTextField(20);
  JButton search = new JButton("Search");
  JPanel text = new JPanel();
  JPanel err = new JPanel();
  JLabel error = new JLabel("");

  // CONSTRUCTOR - Builds the pane and adds actionListener
  public PlaylistSearch(Container cp) {
    cp.removeAll();
    cp.setLayout(new FlowLayout());
    text.setPreferredSize(new Dimension(600,35));
    err.setPreferredSize(new Dimension(600,25));
    text.add(lbl_name);
    text.add(playlist);
    setButton(search, "PlaylistSearch.Search", al);
    text.add(search);
    err.add(error);
    cp.add(text);
    cp.add(err);
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {

    // Only one command - search.
    if (cmd.equals("PlaylistSearch.Search")) {
      // Send the command.
      server.sendCommand("findplaylist@" + playlist.getText() + "@");

      // If can't find it, inform user.
      if (server.reply.size() == 0) error.setText("Playlist not found.");

      // Otherwise, show the playlist details.
      else pane = new PlaylistDetails(cp);
    }
  }
}


// Show a list of MP3's (Artist and title shown)
class MP3List extends DataFrame {
  DefaultListModel mp3Items = new DefaultListModel();
  JList mp3s = new JList(mp3Items);
  JButton showDetail = new JButton("Show Details");
  JTextField txtPL = new JTextField(20);
  JButton addToPL = new JButton("Add to Play list");
  JTextField txtPos = new JTextField(5);
  JButton edit = new JButton("Edit ID3 Tag");
  JButton del = new JButton("Delete from Database");
  JScrollPane list;
  JPanel addpl = new JPanel();
  JPanel buttons = new JPanel();
  JLabel lblPL = new JLabel("Playlist:");
  JLabel lblPos = new JLabel("at Pos:");

  // CONSTRUCTOR - builds the pane and adds actionListener
  public MP3List(Container cp) {

    // Check if there is a playlist in the reply. If not, can't do anything.
    int l = server.reply.size();
    if (l==0) mp3Items.add(0,"No MP3s in this list");
    else {
      for (int i=0; i<l; i++)
        mp3Items.add(i, ((Track)server.reply.get(i)).getArtist() + " - " +
                        ((Track)server.reply.get(i)).getTitle());
    }

    cp.removeAll();
    cp.setLayout(new FlowLayout());
    list = new JScrollPane(mp3s);
    mp3s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setPreferredSize(new Dimension(600,200));
    addpl.setPreferredSize(new Dimension(600,35));
    buttons.setPreferredSize(new Dimension(600,35));
    setButton(addToPL, "MP3List.AddToPlaylist", al);
    addpl.add(addToPL);
    addpl.add(lblPL);
    addpl.add(txtPL);
    addpl.add(lblPos);
    addpl.add(txtPos);
    setButton(showDetail, "MP3List.ShowMP3", al);
    buttons.add(showDetail);
    setButton(edit, "MP3List.Edit", al);
    buttons.add(edit);
    setButton(del, "MP3List.Del", al);
    buttons.add(del);
    cp.add(list);
    cp.add(addpl);
    cp.add(buttons);
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {

    // Firstly, find out which track in the list (if any) was selected.
    // If no valid track was selected, die (do nothing)

    if (txtPos.getText().equals("")) txtPos.setText("1");
    int i = mp3s.getSelectedIndex();
    if (i < 0) return;
    if ((i == 0) && (((String)mp3s.getSelectedValue()).equals("No MP3s in this list"))) return;
    Track tr = (Track)server.reply.get(i);

    // If no track, die.
    if (tr == null) return;

    // Add this track to a playlist. Requires a playlist and a position to add to, although
    // if there's no 'pos', 1 is assumed.
    else if (cmd.equals("MP3List.AddToPlaylist")) {
      if ((!txtPL.getText().equals("")) && (!txtPos.getText().equals(""))) {
        server.sendCommand("addtrack@" + txtPL.getText() + "@" + tr.getArtist() + "@" +
                           tr.getTitle() + "@" + txtPos.getText() + "@");
        pane = new PlaylistDetails(cp);
      }
    }

    // Show the details of this MP3
    else if (cmd.equals("MP3List.ShowMP3")) {
      server.reply.clear();
      server.reply.add(0, tr);
      pane = new MP3Details(cp);
    }

    // Edit this MP3
    else if (cmd.equals("MP3List.Edit")) {
      server.reply.clear();
      server.reply.add(0,tr);
      pane = new AddEditMP3(cp);
    }

    // Delete selected MP3.
    else if (cmd.equals("MP3List.Del")) {
      server.sendCommand("deletetrack@" + tr.getArtist() + "@" + tr.getTitle() + "@");
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }
  }
}


// Only for Fav listing, as searching produces either yes or no
class PlaylistList extends DataFrame {
  DefaultListModel playlistItems = new DefaultListModel();
  JList playlists = new JList(playlistItems);
  JButton showContents = new JButton("Show Contents");
  JButton rmFromFav = new JButton("Remove from Favorites");
  JButton addToFav = new JButton("Add to Favorites");
  JTextField moveswap = new JTextField(3);
  JButton moveTo = new JButton("Move to...");
  JButton swapWith = new JButton("Swap with...");
  JButton edit = new JButton("Edit Playlist Name");
  JButton del = new JButton("Delete Playlist");
  JScrollPane list;
  JPanel pnlMoveSwap = new JPanel();
  JLabel lblPos = new JLabel("Position:");
  JPanel buttons = new JPanel();
  JLabel title = new JLabel("");
  JPanel pnltitle = new JPanel();

  // CONSTRUCTOR - builds the window and adds actionListener
  public PlaylistList(Container cp) {
    // Two cases... this is a complete list
    if (flags == 1) {
      moveTo.setVisible(false);
      swapWith.setVisible(false);
      moveswap.setVisible(false);
      rmFromFav.setVisible(false);
      addToFav.setVisible(true);
      lblPos.setVisible(false);
      title.setText("All Playlists");
    }

    // Or this is a favorites list
    else {
      moveTo.setVisible(true);
      swapWith.setVisible(true);
      moveswap.setVisible(true);
      rmFromFav.setVisible(true);
      addToFav.setVisible(false);
      lblPos.setVisible(true);
      title.setText("Favorites");
    }
    // Those simply hide the unwanted buttons and objects.

    // Get all the playlists from the reply, after sending the server the command
    int l = server.reply.size();
    if (l==0) playlistItems.add(0,"No playlists to view");
    else if (server.reply.get(0) == null) playlistItems.add(0,"No playlists to view");
    else {
      for (int i=0; i<l; i++)
        playlistItems.add(i, ((PlayList)server.reply.get(i)).getName());
    }

    // Main layout starts here
    cp.removeAll();
    cp.setLayout(new FlowLayout());
    list = new JScrollPane(playlists);
    playlists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    pnltitle.setPreferredSize(new Dimension(600,25));
    list.setPreferredSize(new Dimension(600,200));
    buttons.setPreferredSize(new Dimension(600,35));
    pnlMoveSwap.setPreferredSize(new Dimension(600,35));
    pnltitle.add(title);
    setButton(del, "PlaylistList.Del", al);
    buttons.add(del);
    setButton(edit, "PlaylistList.Edit", al);
    buttons.add(edit);
    setButton(showContents, "PlaylistList.ShowContents", al);
    buttons.add(showContents);
    setButton(addToFav, "PlaylistList.addToFav", al);
    buttons.add(addToFav);
    setButton(rmFromFav, "PlaylistList.RmFromFav", al);
    buttons.add(rmFromFav);
    setButton(moveTo, "PlaylistList.MoveTo", al);
    pnlMoveSwap.add(moveTo);
    setButton(swapWith, "PlaylistList.SwapWith", al);
    pnlMoveSwap.add(swapWith);
    pnlMoveSwap.add(lblPos);
    pnlMoveSwap.add(moveswap);
    cp.add(pnltitle);
    cp.add(list);
    cp.add(buttons);
    cp.add(pnlMoveSwap);

    cp.validate();
    cp.repaint();
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {
    // Find out which Playlist has been selected. If no valid playlist was selected, die.
    int i = playlists.getSelectedIndex();
    if (i < 0) return;
    if ((i == 0) && (((String)playlists.getSelectedValue()).equals("No playlists to view"))) return;
    PlayList pl = (PlayList)server.reply.get(i);
    if (pl == null) {return;}

    // Delete the selected playlist from the database
    else if (cmd.equals("PlaylistList.Del")) {
      server.sendCommand("deleteplaylist@" + pl.getName() + "@");
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }

    // Show the contents of the playlist
    else if (cmd.equals("PlaylistList.ShowContents")) {
      server.reply.clear();
      server.reply.add(0, pl);
      pane = new PlaylistDetails(cp);
    }

    // Add this playlist to my favorites
    else if (cmd.equals("PlaylistList.addToFav")) {
      server.sendCommand("addplaylist@" + pl.getName() + "@1@");
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }

    // Remove this playlist from my favorites.
    else if (cmd.equals("PlaylistList.RmFromFav")) {
      server.sendCommand("removeplaylist@" + (i+1) + "@");
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }

    // Move the playlists position in the list
    else if (cmd.equals("PlaylistList.MoveTo")) {
      try {
        int ip = (new Integer(moveswap.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Position!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (moveswap.getText().equals("")) return;
      else {
        server.sendCommand("moveplaylist@" + (i+1) + "@" + moveswap.getText() + "@");
        server.sendCommand("getfavorites@");
        pane = new PlaylistList(cp);
      }
    }

    // Swap two playlists in the list over
    else if (cmd.equals("PlaylistList.SwapWith")) {
      try {
        int ip = (new Integer(moveswap.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Position!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (moveswap.getText().equals("")) {return;}
      else {
        server.sendCommand("swapplaylist@" + (i+1) + "@" + moveswap.getText() + "@");
        server.sendCommand("getfavorites@");
        pane = new PlaylistList(cp);
      }
    }

    // Edit the selected playlists name
    else if (cmd.equals("PlaylistList.Edit")) {
      server.reply.clear();
      server.reply.add(0, pl);
      pane = new AddEditPlaylist(cp);
    }
  }
}


// Show the MP3 Details
class MP3Details extends DataFrame {
  Track tr;
  JLabel lblArtist = new JLabel("Artist: ");
  JLabel lblTitle = new JLabel("Title: ");
  JLabel lblFilename = new JLabel("Filename: ");
  JLabel lblAlbum = new JLabel("Album: ");
  JLabel lblGenre = new JLabel("Genre: ");
  JLabel lblSize = new JLabel("Size: ");
  JLabel lblLength = new JLabel("Length: ");
  JLabel lblBitrate = new JLabel("Bitrate: ");
  JTextField playlist = new JTextField(20);
  JButton addToPL = new JButton("Add to Playlist");
  JTextField plPos = new JTextField(5);
  JButton edit = new JButton("Edit ID3 Tag");
  JButton del = new JButton("Delete from Database");
  JPanel pnlList[] = new JPanel[10];
  JLabel lblPL = new JLabel("Playlist:");
  JLabel lblPos = new JLabel("Position:");

  // CONSTRUCTOR - build the pane and add the actionListener
  public MP3Details(Container cp) {
    cp.removeAll();
    cp.setLayout(new FlowLayout());
    for (int i=0; i<8; i++) {
      pnlList[i] = new JPanel();
      pnlList[i].setPreferredSize(new Dimension(600,20));
    }
    for (int i=8; i<10; i++) {
      pnlList[i] = new JPanel();
      pnlList[i].setPreferredSize(new Dimension(600,35));
    }
    pnlList[0].add(lblArtist);
    pnlList[1].add(lblTitle);
    pnlList[2].add(lblFilename);
    pnlList[3].add(lblAlbum);
    pnlList[4].add(lblGenre);
    pnlList[5].add(lblSize);
    pnlList[6].add(lblLength);
    pnlList[7].add(lblBitrate);
    setButton(addToPL, "MP3Details.AddToPL", al);
    pnlList[8].add(addToPL);
    pnlList[8].add(lblPL);
    pnlList[8].add(playlist);
    pnlList[8].add(lblPos);
    pnlList[8].add(plPos);
    setButton(edit, "MP3Details.Edit", al);
    pnlList[9].add(edit);
    setButton(del, "MP3Details.Del", al);
    pnlList[9].add(del);
    for (int i=0; i<10; i++) cp.add(pnlList[i]);

    // Check to see if there's a track in the reply
    if (server.reply.size() > 0) {
      tr = (Track)server.reply.get(0);
      // got the track, tr... now to extract the info!!

      lblArtist.setText(lblArtist.getText() + tr.getArtist());
      lblTitle.setText(lblTitle.getText() + tr.getTitle());
      lblFilename.setText(lblFilename.getText() + tr.getFilename());
      lblAlbum.setText(lblAlbum.getText() + tr.getAlbum());
      lblGenre.setText(lblGenre.getText() + tr.getGenre());
      lblSize.setText(lblSize.getText() + tr.getFileSize());
      lblLength.setText(lblLength.getText() + tr.getLength());
      lblBitrate.setText(lblBitrate.getText() + tr.getBitRate());
    }
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {
    // The track show is already in a variable, tr.
    if (tr == null) return;	// If tr is not a track, die.

    // Add this track to a playlist.
    if (cmd.equals("MP3Details.AddToPL")) {
      if (playlist.getText().equals("")) { return; }
      else {
        if (plPos.getText().equals("")) plPos.setText("1");
        server.sendCommand("addtrack@" + playlist.getText() + "@" +
                           tr.getArtist() + "@" + tr.getTitle() + "@" +
                           plPos.getText() + "@");
        pane = new PlaylistDetails(cp);
      }
    }

    // Edit the details of this track
    else if (cmd.equals("MP3Details.Edit")) {
      server.reply.clear();
      server.reply.add(0, tr);
      pane = new AddEditMP3(cp);
    }

    // Delete this track from the database
    else if (cmd.equals("MP3Details.Del")) {
      server.sendCommand("deletetrack@" + tr.getArtist() + "@" + tr.getTitle() + "@");
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }
  }
}


// Playlist details - this means the list of MP3s in the playlist!
class PlaylistDetails extends DataFrame {
  PlayList pl;
  DefaultListModel mp3Items = new DefaultListModel();
  JList mp3s = new JList(mp3Items);
  JButton edit = new JButton("Edit Playlist Name");
  JButton del = new JButton("Delete Playlist");
  JButton showDetails = new JButton("Show MP3 Details");
  JButton rmFromPlaylist = new JButton("Remove MP3 from Playlist");
  JButton addToFav = new JButton("Add to Favorites");

  JTextField pos = new JTextField(5);
  JButton swapWith = new JButton("Swap with...");
  JButton moveTo = new JButton("Move to...");

  JTextField appendListName = new JTextField(20);
  JButton appendList = new JButton("Append to...");

  JTextField duplnewname = new JTextField(20);
  JButton duplicate = new JButton("Duplicate...");

  JScrollPane pnllist;
  JPanel pnlbuttons = new JPanel();
  JPanel pnlmoveswap = new JPanel();
  JPanel pnlAppend = new JPanel();
  JPanel pnldup = new JPanel();

  JLabel lblMovePos = new JLabel("Position:");
  JLabel lblAppendPL = new JLabel("Append to Playlist:");
  JLabel lblDupPL = new JLabel("New Playlist Name:");
  JLabel title = new JLabel("");
  JPanel pnltitle = new JPanel();


  // CONSTRUCTOR - build the pane and add the actionListener
  public PlaylistDetails(Container cp) {
    // get the playlist object from the previous reply
    if (server.reply.size() != 0) {
      pl = (PlayList)server.reply.get(0);
      title.setText(pl.getName());
      // send a new command to get the playlists
      if (server.sendCommand("gettracks@" + pl.getName() + "@") != server.CONNECTED) {
        pane = new SessionDisconnect(cp);
        server.shutdown();
      }
    } else title.setText("No playlist.");

    // List all the MP3s in this Playlist
    int l = server.reply.size();
    if (l == 0) mp3Items.add(0, "No MP3s in this list");
    else {
      for (int i=0; i<l; i++)
        mp3Items.add(i, ((Track)server.reply.get(i)).getArtist() + " - " +
                        ((Track)server.reply.get(i)).getTitle());
    }

    cp.removeAll();
    cp.setLayout(new FlowLayout());

    pnllist = new JScrollPane(mp3s);
    mp3s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mp3s.setSelectedIndex(0);
    pnltitle.setPreferredSize(new Dimension(600,25));
    pnllist.setPreferredSize(new Dimension(600,200));

    pnlbuttons.setPreferredSize(new Dimension(600,35));
    pnlmoveswap.setPreferredSize(new Dimension(600,35));
    pnlAppend.setPreferredSize(new Dimension(600,35));
    pnldup.setPreferredSize(new Dimension(600,35));    
    pnltitle.add(title);
    setButton(edit, "PlaylistDetails.Edit", al);
    setButton(del, "PlaylistDetails.Del", al);
    setButton(del, "PlaylistDetails.DelTrack", al);
    setButton(addToFav, "PlaylistDetails.AddToFav", al);
    setButton(showDetails, "PlaylistDetails.ShowDetail", al);
    setButton(rmFromPlaylist, "PlaylistDetails.RmFromPlaylist", al);
    setButton(moveTo, "PlaylistDetails.MoveTo", al);
    setButton(swapWith, "PlaylistDetails.SwapWith", al);
    setButton(appendList, "PlaylistDetails.AppendList", al);
    setButton(duplicate, "PlaylistDetails.Duplicate", al);
    pnlbuttons.add(edit);
    pnlbuttons.add(del);
    pnlbuttons.add(addToFav);
    pnlbuttons.add(showDetails);
    pnlmoveswap.add(rmFromPlaylist);
    pnlmoveswap.add(moveTo);
    pnlmoveswap.add(swapWith);
    pnlmoveswap.add(lblMovePos);
    pnlmoveswap.add(pos);
    pnlAppend.add(appendList);
    pnlAppend.add(lblAppendPL);
    pnlAppend.add(appendListName);
    pnldup.add(duplicate);
    pnldup.add(lblDupPL);
    pnldup.add(duplnewname);
    cp.add(pnltitle);
    cp.add(pnllist);
    cp.add(pnlbuttons);
    cp.add(pnlmoveswap);
    cp.add(pnlAppend);
    cp.add(pnldup);
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {
    // Find out if there are any MP3's selected... if not, die.
    int i = mp3s.getSelectedIndex();
    boolean trackcmd = true;
    if (pl == null) return;
 
    // If the track HAS been selected, AND the command is one which works with tracks...
    else if ((i > 0) || (!((String)mp3s.getSelectedValue()).equals("No MP3s in this list"))) {
      // Retreive the track out of the reply
      Track tr = (Track)server.reply.get(i);

      // Show the details of the track
      if (cmd.equals("PlaylistDetails.ShowDetail")) {
        server.reply.clear();
        server.reply.add(0, tr);
        pane = new MP3Details(cp);
      }

      // Remove this track from the playlist
      else if (cmd.equals("PlaylistDetails.RmFromPlaylist")) {
        server.sendCommand("removetrack@" + pl.getName() + "@" + (i+1) + "@");
        pane = new PlaylistDetails(cp);
      }

      // Move the selected track to a new position in the playlist
      else if (cmd.equals("PlaylistDetails.MoveTo")) {
        try {
          int ip = (new Integer(pos.getText())).intValue();
          server.sendCommand("movetrack@" + pl.getName() + "@" + (i+1) + "@" + pos.getText() + "@");
          pane = new PlaylistDetails(cp);
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(null, "Enter a number in Position!", "Oops!", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }

      // Swap two tracks over in the playlist
      else if (cmd.equals("PlaylistDetails.SwapWith")) {
        try {
          int ip = (new Integer(pos.getText())).intValue();
          server.sendCommand("swaptrack@" + pl.getName() + "@" + (i+1) + "@" + pos.getText() + "@");
          pane = new PlaylistDetails(cp);
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(null, "Enter a number in Position!", "Oops!", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }

    // If this is a playlist command, don't need the track
    // Edit the playlist name
    if (cmd.equals("PlaylistDetails.Edit")) {
      server.reply.clear();
      server.reply.add(0, pl);
      pane = new AddEditPlaylist(cp);
    }

    // Delete this playlist from the database
    else if (cmd.equals("PlaylistDetails.Del")) {
      server.sendCommand("deleteplaylist@" + pl.getName() + "@");
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp); 
    }

    // Add this playlist to your favorites
    else if (cmd.equals("PlaylistDetails.AddToFav")) {
      server.sendCommand("addplaylist@" + pl.getName() + "@1@");
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }

    // Append one playlist to another (specified)
    else if (cmd.equals("PlaylistDetails.AppendList")) {
      if (appendListName.getText().equals("")) return;
      server.sendCommand("appendplaylist@" + appendListName.getText() + "@" + pl.getName() + "@");
      pane = new PlaylistDetails(cp);
    }

    // Duplicate the playlist and give the new one a new name
    else if (cmd.equals("PlaylistDetails.Duplicate")) {
      if (duplnewname.getText().equals("")) return;
      server.sendCommand("copyplaylist@" + pl.getName() + "@" + duplnewname.getText() + "@");
      pane = new PlaylistDetails(cp);
    }
  }
}

// Simple editing pane for MP3s
class AddEditMP3 extends DataFrame {
  Track tr;
  JLabel lblArtist = new JLabel("Artist:");
  JTextField artist = new JTextField(20);
  JLabel lblTitle = new JLabel("Title:");
  JTextField title = new JTextField(20);
  JLabel lblFilename = new JLabel("Filename:");
  JTextField filename = new JTextField(20);
  JLabel lblAlbum = new JLabel("Album:");
  JTextField album = new JTextField(20);
  JLabel lblGenre = new JLabel("Genre:");
  JTextField genre = new JTextField(20);
  JLabel lblSize = new JLabel("Size:");
  JTextField size = new JTextField(20);
  JLabel lblLength = new JLabel("Length:");
  JTextField length = new JTextField(20);
  JLabel lblBitrate = new JLabel("Bitrate:");
  JTextField bitrate = new JTextField(20);

  JButton save = new JButton("Save");

  JPanel[] pnlList = new JPanel[8];

  // CONSTRUCTOR - build the pane and add the actionListener
  public AddEditMP3(Container cp) {
    // Check if this is add or edit and get the track to be edited
    if (server.reply.size() > 0) {
      tr = (Track)server.reply.get(0);
      artist.setText(tr.getArtist());
      title.setText(tr.getTitle());
      filename.setText(tr.getFilename());
      album.setText(tr.getAlbum());
      genre.setText(tr.getGenre());
      size.setText(new Integer(tr.getFileSize()).toString());
      length.setText(new Integer(tr.getLength()).toString());
      bitrate.setText(new Integer(tr.getBitRate()).toString());
    }
    cp.removeAll();
    cp.setLayout(new FlowLayout());
    for (int i=0; i<8; i++) {
      pnlList[i] = new JPanel();
      pnlList[i].setPreferredSize(new Dimension(600,25));
    }
    pnlList[0].add(lblArtist);
    pnlList[0].add(artist);
    pnlList[1].add(lblTitle);
    pnlList[1].add(title);
    pnlList[2].add(lblFilename);
    pnlList[2].add(filename);
    pnlList[3].add(lblAlbum);
    pnlList[3].add(album);
    pnlList[4].add(lblGenre);
    pnlList[4].add(genre);
    pnlList[5].add(lblSize);
    pnlList[5].add(size);
    pnlList[6].add(lblLength);
    pnlList[6].add(length);
    pnlList[7].add(lblBitrate);
    pnlList[7].add(bitrate);
    for (int i=0; i<8; i++) cp.add(pnlList[i]);
    setButton(save, "AddEditMP3.Save", al);
    cp.add(save);
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {
    // Save the MP3 details... first, check to see if any empty string
    if (cmd.equals("AddEditMP3.Save")) {
      if (artist.getText().equals("")) artist.setText("?");
      if (title.getText().equals("")) title.setText("?");
      if (filename.getText().equals("")) filename.setText("?");
      if (album.getText().equals("")) album.setText("?");
      if (genre.getText().equals("")) genre.setText("?");
      if (size.getText().equals("")) size.setText("0");
      if (length.getText().equals("")) length.setText("0");
      if (bitrate.getText().equals("")) bitrate.setText("0");

      try {
        int ip = (new Integer(size.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Filesize!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      try {
        int ip = (new Integer(length.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Length!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      try {
        int ip = (new Integer(bitrate.getText())).intValue();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Enter a number in Bit Rate!", "Oops!", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // if there is nothing in 'reply', the this is an add
      if (tr == null) {	// Add new track
        if (server.sendCommand("createtrack@" + artist.getText() + "@" + title.getText() + "@" +
                           filename.getText() + "@" + album.getText() + "@" + genre.getText() +
                           "@" + size.getText() + "@" + length.getText() + "@" + bitrate.getText() +
                           "@") != server.CONNECTED) {
          pane = new SessionDisconnect(cp);
          server.shutdown();
        }
        else pane = new MP3Details(cp);
      }

      // Otherwise, edit the track that's in the playlist
      else {
        if (server.sendCommand("edittrack@" + tr.getArtist() + "@" + tr.getTitle() +
                           "@" + artist.getText() + "@" + title.getText() + "@" +
                           filename.getText() + "@" + album.getText() + "@" + genre.getText() +
                           "@" + size.getText() + "@" + length.getText() + "@" + bitrate.getText() +
                           "@") != server.CONNECTED) {
          pane = new SessionDisconnect(cp);
          server.shutdown();
        }
        else pane = new MP3Details(cp);
      }
    }
  }
}


class AddEditPlaylist extends DataFrame {
  PlayList pl=null;
  JLabel lbl_name = new JLabel("Playlist Name:");
  JTextField playlist = new JTextField(20);
  JButton save = new JButton("Save");

  // CONSTRUCTOR - build the pane and add the actionListener 
  public AddEditPlaylist(Container cp) {
    // See if this is an add or an edit. If edit, get PlayList to be edited.
    if (server.reply.size() > 0) { pl = (PlayList)server.reply.get(0); playlist.setText(pl.getName()); }
    cp.removeAll();
    cp.setLayout(new FlowLayout());
    cp.add(lbl_name);
    cp.add(playlist);
    setButton(save, "AddEditPlaylist.Save", al);
    cp.add(save);
  }


  // doAction runs when a button in THIS PANE is clicked.
  // The cmd is given by the 2nd argument of setButton (above)
  void doAction(String cmd) {
    // Only one command, either edit the playlist in reply, or add new one
    if (!playlist.getText().equals("")) {
      if (cmd.equals("AddEditPlaylist.Save")) {
        if (pl != null) server.sendCommand("setname@" + pl.getName() + "@" + playlist.getText() + "@");
        else server.sendCommand("createplaylist@" + playlist.getText() + "@");
        pane = new PlaylistDetails(cp);
      }
    }
  }
}

// Tell user that you've been disconnected.
class SessionDisconnect extends DataFrame {
  public SessionDisconnect(Container cp) {
    cp.removeAll();
    cp.add(new JLabel("Disconnected! Click Reload/Refresh on your Browser to restart the Applet."));
  }
}




public class MP3Magic extends JApplet {
  // Subcompontents of the MP3Magic Applet.
  // Display pane
  protected static DataFrame pane;
  protected static User me;
  protected static ConnectionMng server;
  protected static JPanel cp = new JPanel();
  protected static int flags = 0;
  
  // Handles events for the Applet
  static ActionListener al = new ActionListener() {
    public void actionPerformed(ActionEvent e) {

      // First, get the command string associated with action (helpfull - it'll tell us what to do!)
      String cmd = new String(((AbstractButton)e.getSource()).getActionCommand());

      // Check to see if the command is 'legal' (ie the person is logged or logging on)
      if ((me != null) || (cmd.equals("SessionLogin.login"))) {
        // Call the doAction() command. This runs in the class where the event occured, ie:
        // Menu items: MP3Magic.doAction(cmd) runs,
        // Session Login pane: SessionLogin.doAction(cmd) runs, etc..
        if (pane != null) pane.doAction(cmd);	// Pane classes. Controls sending of commands
        doAction(cmd);		// This class. Controls the changing of panes
        flags = 0;
      }

    }
  };
  
  // Set up menus. Messy - lots of variables!!
  JMenu[] menus = {
    new JMenu("Session"),
    new JMenu("MP3"),
    new JMenu("Playlist"),
  };
  JMenuItem[] sessionItems = {
    new JMenuItem("Log out")
  };
  JMenuItem[] mp3Items = {
    new JMenuItem("Search"),
    new JMenuItem("Add"),
    new JMenuItem("Show All")
  };
  JMenuItem[] playlistItems = {
    new JMenuItem("Search"),
    new JMenuItem("Goto Favorites"),
    new JMenuItem("Add new Playlist"),
    new JMenuItem("Show All")
  };


  // Initialise the program! This runs when the class is first initialised.
  public void init() {
    // Create a static Content Pane that any subclass can access
    setContentPane(cp);

    // Send a new SessionLogin. This will repaint the pane for the Session Login screen.
    pane = new SessionLogin(cp);    

    // Set up Connection Manager and the server. If it can't connect, ABORT, ABORT!!
    server = new ConnectionMng();
    if (server.connect() != server.CONNECTED) {
      cp.removeAll();
      cp.add(new JLabel("Can't connect to server. Please try again later."));
    }

    // Build menus - again, messy, but useful.
    for (int i=0; i<sessionItems.length; i++) {
      menus[0].add(sessionItems[i]);
      sessionItems[i].setActionCommand("Session " + i); // Give the ActionCommand a useful name!
      sessionItems[i].addActionListener(al);		// Adds the Applet ActionListener to the menu
    }

    for (int i=0; i<mp3Items.length; i++) {
      menus[1].add(mp3Items[i]);
      mp3Items[i].setActionCommand("MP3 " + i);
      mp3Items[i].addActionListener(al);
    }
    for (int i=0; i<playlistItems.length; i++) {
      menus[2].add(playlistItems[i]);
      playlistItems[i].setActionCommand("Playlist " + i);
      playlistItems[i].addActionListener(al);
    }
    JMenuBar menuBar = new JMenuBar();
    for (int i=0; i<menus.length; i++) menuBar.add(menus[i]);
    setJMenuBar(menuBar);    
    // Program stops here until an ActionEvent has been received.
  }


  // Control changing panes from menus
  private static void doAction(String cmd) {
    if (server.status != server.CONNECTED) {
      pane = new SessionDisconnect(cp);
      server.shutdown();
    }
    else if (cmd.equals("Session 0")) pane = new SessionLogout(cp);
    else if (cmd.equals("MP3 0")) pane = new MP3Search(cp);
    else if (cmd.equals("MP3 1")) {
      server.reply.clear();
      pane = new AddEditMP3(cp);
    }
    else if (cmd.equals("MP3 2")) {
      server.sendCommand("getalltracks@");
      pane = new MP3List(cp);
    }
    else if (cmd.equals("Playlist 0")) pane = new PlaylistSearch(cp);
    else if (cmd.equals("Playlist 1")) {
      server.sendCommand("getfavorites@");
      pane = new PlaylistList(cp);
    }
    else if (cmd.equals("Playlist 2")) {
      server.reply.clear();
      pane = new AddEditPlaylist(cp);
    }
    else if (cmd.equals("Playlist 3")) {
      server.sendCommand("getallplaylists@");
      flags = 1; // Signal no move/swap
      pane = new PlaylistList(cp);
    }

    // Just update the window so that the pane is 'repainted' and visible to the user.
    cp.validate();
    cp.repaint();
  }

  // Destroy the Applet
  public void destroy() {
    if (server.status == server.CONNECTED) {
      server.sendCommand("logout@f@");
    }
    server.shutdown();
  }
}
