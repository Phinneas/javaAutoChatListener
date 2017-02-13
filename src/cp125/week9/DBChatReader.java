package cp125.week9;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * @author Chester Beard
 * @version 06/08/2014
 *
 * Connect to a 'Derby/JavaDB' database on localhost.  Inspect tables in
 * a 'chat' database, creating the schema tables if required.
 *
 * A support program for the week 9 assignment, but NOT an active
 * 'chat component'.  Rather, reads and prints out chat data
 * previously recorded by the DBChat program.
 *
 * To run, we need our classpath to include the derby driver jar, e.g.
 *
 * java -cp derbyclient.jar:bin cp125.week9.DBChatReader
 *
 * @see DBChat
 */

public class DBChatReader {

	static public void main( String[] args ) {

		String host = "127.0.0.1";
		String database = "chat";
		
		try {
			// url includes host and database...
			Connection c = DriverManager.getConnection
				( "jdbc:derby://" + host + "/" + database + ";create=true" );
			DBChat.createSchema( c );
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery( "select * from conversations " +
										   "order by id" );
			// loop over all conversations...
			while( rs.next() ) {
				Timestamp startTime = rs.getTimestamp( "id" );
				String peer = rs.getString( "peer" );
				System.out.println();
				System.out.println( "Conversation started " + startTime +
									", with " + peer );
				showConversation( startTime, c );
				System.out.println();
			}
										   
			c.close();
		} catch( SQLException se ) {
			System.err.println( se );
		}
	}

	static void showConversation( Timestamp id, Connection c ) {
		try {
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery
				( "select * from conversation where id = '" + id + "' " +
				  "order by when" );
			while( rs.next() ) {
				String who = rs.getString( "who" );
				Timestamp when = rs.getTimestamp( "when" );
				String what = rs.getString( "what" );
				System.out.println( who + " at " + when );
				System.out.println( "  " + what );
			}
		} catch( SQLException se ) {
			System.err.println( se );
		}			
	}							 

}

// eof

