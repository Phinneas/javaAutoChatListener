package cp125.week9;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * @author Chester Beard
 * @version 06/08/2014
 *
 * A helper application for the week 9 assignment.  Acts as a ChatListener,
 * yet doesn't require any human user.  Instead, reads text from the
 * peer over the socket connection and replies with a response chosen
 * randomly from a set of canned strings.
 *
 * To run: AutoChatListener port
 */

public class AutoChatListener {

	@SuppressWarnings("resource")
	static public void main( String[] args ) {

		String usage = "Usage: " + AutoChatListener.class.getName() + " port";

		if( args.length < 1 ) {
			System.err.println( usage );
			System.exit(1);
		}

		int port = -1;
		try {
			port = Integer.parseInt( args[0] );
		} catch( NumberFormatException nfe ) {
			System.err.println( usage );
			System.exit(1);
		}

		ServerSocket ss = null;
		try {
			ss = new ServerSocket( port );		//need command to server before 'ss' can be closed
		} catch( IOException ioe ) {
			System.err.println( ioe );
			System.exit(1);
		
		}
		System.out.println( "Listening: " + ss );
		
		while( true ) {
			try {
				Socket s = ss.accept();
				System.out.println( "Connected: " + s );
				Worker w = new Worker( s );
				new Thread( w ).start();
			} catch( IOException ioe ) {
				System.err.println( ioe );
				
			}
		}
	}

	static class Worker implements Runnable {
		Worker( Socket s ) {
			this.s = s;
			rnd = new Random();
		}
		
		@Override
		public void run() {
			try {
				autoChat( s );
				s.close();
			} catch( IOException ioe ) {
				System.err.println( ioe );
			}
		}

		void autoChat( Socket s ) throws IOException {
			// read lines from the peer and write back random stuff...
			InputStream source = s.getInputStream();
			BufferedReader br = new BufferedReader
				( new InputStreamReader( source ) );

			// output back to client.  text, so need a writer
			OutputStream sink = s.getOutputStream();
			PrintWriter pw = new PrintWriter( sink, true );

			while( true ) {
				try {
					String line = br.readLine();
					if( line == null )
						break;
					System.out.println( "Peer: " + line );
					String response = randomResponse();
					System.out.println( "Auto: " + response );
					pw.println( response );
				} catch( IOException ioe ) {
					break;
				}
			}
		}

		String randomResponse() {
			int n = rnd.nextInt( STRINGS.length );
			return STRINGS[n];
		}

		static private final String[] STRINGS = {
			"How do you do?",
			"This Java class is way too looooooooong.",
			"Are you watching the World Cup?",
			"I like this chat program, I bet it's in Java.",
			"I wonder if we do any Cobol in this class?",
			"Java 8 is out you know, has closures, hurrah!",
			"I think we're learning Java and databases today.",
			"JDBC is great!"
		};
		
		private final Socket s;
		private final Random rnd;
	}


}

// eof

