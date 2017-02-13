package cp125.week9;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * @author Chester Beard
 * @version 06/08/2014
 *
 * The main file for the week 9 assignment (Java/Databases).

 * First connect to a 'Derby/JavaDB' database on localhost (or some
 * local/remote Mysql if you prefer and can edit the db url
 * accordingly).  As part of the connection set up, we create a schema of
 * two tables:
 *
 * 1: conversations - A record of all chat sessions.  Record with whom
 * we spoke (just Internet host name, not a user),and when each
 * conversation started.
 *
 * 2: conversation - For a single chat session, record who said what and when.
 *
 * We then connect via tcp sockets to some chat listener (perhaps the
 * AutoChatListener?) and have a two-way chat. We are multi-threaded,
 * so can handle either us or the peer saying things at any time.
 *
 * When either user speaks, we transmit their spoken data across the
 * network to the peer.  We also want to RECORD the spoken text in our
 * database.
 *
 * The 'chat' logic is mostly already written.  What remains is to
 * fill in the missing logic in two methods:
 *
 * 1: startConversation - where we want to record, in the 'conversations' table,
 * the host we connected to and when we connected.
 *
 * 2: readWriteLoop - where we want keep reading text from a buffered
 * reader and writing to a printwriter as long as the reader has more
 * data.  In one thread, the reader will be attached to the local
 * keyboard and will thus manage data from the local user.  In the other thread,
 * the reader will be attached to the socket and will thus manage data from
 * the peer chat user.
 *
 * For every line read, we want to record it in the 'conversation'
 * table, along with who said it and exactly when they said it.
 *
 * To run, we need our classpath to include the derby driver jar, e.g.
 *
 * java -cp derbyclient.jar:bin cp125.week9.DBChat listeningHost listeningPort
 *
 * @see DBChatReader - for a printout of all stored conversations
 * @see AutoChatListener - for a chat listener which replies with canned text
 */

public class DBChat {

	static public void main( String[] args ) {

		String usage = "Usage: " + DBChat.class.getName() + " host port";

		if( args.length < 2 ) {
			System.err.println( usage );
			System.exit(1);
		}

		String host = args[0];
		int port = 0;
		try {
			port = Integer.parseInt( args[1] );
		} catch( NumberFormatException nfe ) {
			System.err.println( usage );
			System.exit(1);
		}

		Connection c = null;
		try {
			c = getConnection();
		} catch( SQLException se ) {
			System.err.println( se );
			System.exit(1);
		}
		
		try {
			Socket s = new Socket( host, port );
			System.out.println( "Connected: " + s );
			Timestamp start = new Timestamp( System.currentTimeMillis() );
			startConversation( host, start, c );
			chat( s, start, host, c );

		} catch( IOException ioe ) {
			System.err.println( ioe );
		}
	}


	/**
	 * @parameter conversationID - a moment in time used as a both the
	 * primary key for the conversations table and as the foreign key
	 * in the conversation table.
	 *
	 * @parameter peer - the host we connected to. 
	 */
	static void chat( Socket s,
					  final Timestamp conversationID,
					  final String peer,
					  final Connection c ) throws IOException {
		try {
			/*
			  The new Thread we shall start will take care of data from
			  the peer (the remote program).  Think of it as the
			  'receiving thread'. So the inputstream is the socket, the
			  outputstream is the screen.  Since these 2 variables used in
			  an anonymous implementation of Runnable, they must be
			  declared final
			*/
			final PrintWriter pwLocal = buildWriter( System.out );
			final BufferedReader brRemote = buildReader( s.getInputStream() );
			Runnable r = new Runnable() {
					public void run() {
						readWriteLoop( brRemote, pwLocal, conversationID,
									   peer, c );
						System.out.println( "Peer gone..." );
					}
				};
			Thread t = new Thread( r );
			t.setDaemon( true );
			t.start();
			
			/*
			  The current thread (i.e. the one that called main) will
			  take care of data going to the peer (the remote program).
			  Think of it as the 'sending thread'. So the
			  inputstream is the keyboard, the outputstream is the
			  socket.
			*/
			BufferedReader brLocal = buildReader( System.in );
			PrintWriter pwRemote = buildWriter( s.getOutputStream() );
			readWriteLoop( brLocal, pwRemote, conversationID, "me", c );
		} catch( IOException ioe ) {
			System.err.println( ioe );
		}
	}

	/**
	 * @throws SQLException 
	 */
	static void startConversation( String peer, Timestamp start,
								   Connection c ) {
		/*
		  FILL ME IN.  Need to add a row to the 'conversation'
		  table. Add the row such that the startTime and peer columns
		  are both filled in.
		  
		  We'll use a java.sql.PreparedStatement object to do the insert
		*/
		PreparedStatement newConversationStatement = null;
		String newConversationString = "INSERT INTO conversations (id, peer) VALUES (?, ?)";
		try {
			newConversationStatement = c.prepareStatement(newConversationString);
			newConversationStatement.setTimestamp(1, start);
			newConversationStatement.setString(2, peer);
			newConversationStatement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


 	static void readWriteLoop( BufferedReader br, PrintWriter pw,
							   Timestamp conversationID, String speaker,
							   Connection c ) {
		/*
		  FILL ME IN.  Need to create a java.sql.PreparedStatement to
		  insert rows in the 'conversation' table.  Then need a loop
		  reading from the BufferedReader until no more data (null
		  from readLine()) and writing to pw AND recorded in the db.
		  Each row added needs the conversationID field as a reference
		  back the parent 'conversations' table.
		*/
 		PreparedStatement newConversationLineStatement = null;
 		String newConversationLineStatementString = "INSERT INTO conversation (id, when, who, what) VALUES (?, ?, ?, ?)";
 		try {
			newConversationLineStatement = c.prepareStatement(newConversationLineStatementString);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			String line;
			try {
				line = br.readLine();
				Timestamp ts = new Timestamp( System.currentTimeMillis() );
				try{
				newConversationLineStatement.setTimestamp(1, conversationID);
				newConversationLineStatement.setTimestamp(2, ts);
				newConversationLineStatement.setString(3, speaker);
				newConversationLineStatement.setString(4, line);
				newConversationLineStatement.execute();
				}catch(SQLException e){
					e.printStackTrace();
				}
			
				if (line == null || line.equals("exit")) {
					System.exit(0);
				}
				pw.println(line);

			} catch (IOException e) {
				System.out.println(e);
				System.exit(0);
			}
		}
 		
 		
	}
	
	

	static Connection getConnection() throws SQLException {

		String host = "127.0.0.1";
		String database = "chat";

		/*
		  url includes host and database. No need for user credentials
		  with derby, likely NOT so with Mysql
		*/
		Connection c = DriverManager.getConnection
			( "jdbc:derby://" + host + "/" + database + ";create=true" );
		createSchema( c );
		return c;
	}
		
	static void createSchema( Connection c ) throws SQLException {
		Statement s = c.createStatement();
		try {
			s.executeUpdate( "create " + CONVERSATIONS );
		} catch( SQLException se ) {
			// If the table already exists, we'll see this
			System.err.println( se );
		}

		try {
			s.executeUpdate( "create " + CONVERSATION );
		} catch( SQLException se ) {	
			// If the table already exists, we'll see this
			System.err.println( se );
		}
		
	}

	static BufferedReader buildReader( InputStream is )
		throws IOException {
		return new BufferedReader
			( new InputStreamReader( is ) );
	}

	static PrintWriter buildWriter( OutputStream os )
		throws IOException {
		boolean autoFlush = true;
		return new PrintWriter( os, autoFlush);
	}
	
	static private final String CONVERSATIONS = "table conversations(" +
		"id timestamp primary key," +
		"peer varchar(32) not null" +
		")";

	static private final String CONVERSATION = "table conversation(" +
		"id timestamp references conversations," +
		"who varchar(32) not null," +
		"when timestamp not null," +
		"what varchar(255) not null" +
		")";
}

// eof

