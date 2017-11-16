package com.ibm.ivr.eus.websv;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

//
// TimedGetSocket.java
// 
//

/**
  * @doc 
  * The standard Java get socket: Socket skt = new Socket(hostname,port); does not allow 
  * configuring a timeout.
  * 
  * This class creates a separate thread to time the socket creation
  * 
  * @usage  Socket s = TimedGetSocket.timedGetSocket(strHostname,iPort,iTimeoutSeconds);
  * 	
  *
  */
public class TimedGetSocket
{
	// main thread delay for socket timeout checks (in milliseconds)
	private static final int POLL_DELAY = 100;
	/**
	  * Attempts to connect to a service at the specified address
	  * and port, for a specified maximum amount of time.
	  *
	  *	@param	addr	Address of host
	  *	@param	port	Port of service
	  * @param	delay	Delay in milliseconds
	  */
	public static Socket timedGetSocket ( InetAddress addr, int port, int delay) throws InterruptedIOException, IOException
	{
		// Create a new socket thread, and start it running
		try {
			SocketThread st = new SocketThread( addr, port );
		
		st.start();

		int timer = 0;
		Socket sock = null;

		for (;;)
		{
			// Check to see if a connection is established
			if (st.isConnected())
			{
				// Yes ...  assign to sock variable, and break out of loop
				sock = st.getSocket();
				break;
			}
			else
			{
				// Check to see if an error occurred
				if (st.isError())
				{
					// No connection could be established
					throw (st.getException());
				}

				try
				{
					// Sleep for a short period of time
					Thread.sleep ( POLL_DELAY );
				}
				catch (InterruptedException ie) {}

				// Increment timer
				timer += POLL_DELAY;

				// Check to see if time limit exceeded
				if (timer > delay)
				{
					// Can't connect to server
					throw new InterruptedIOException("Could not connect for " + delay + " milliseconds");
				}
			}
		}

		return sock;
		} catch (IOException ioe) {
			throw new IOException ("Could not connect to "+addr+":"+port);
		}
	}

	/**
	  * Connect to service
	  * at the specified address and port, 
	  * within a specified maximum amount of time.
	  *
	  *	@param	host	Hostname of machine
	  *	@param	port	Port of service
	  * @param	delay	Delay in milliseconds
	  */
	public static Socket timedGetSocket ( String host, int port, int delay) throws InterruptedIOException, IOException
	{
		// Convert host into an InetAddress, and call getSocket method
		InetAddress inetAddr = InetAddress.getByName (host);

		return timedGetSocket ( inetAddr, port, delay );
	}

	

	// Establish a thread within main thread, to attempt the socket creation 
	// which prevents blocking timeout detection in the main thread.
	static class SocketThread extends Thread
	{
		// Socket connection to remote host
		volatile private Socket m_connection = null;
		// Host to connect to
		private String m_host       = null;
		// Internet Address to connect to
		private InetAddress m_inet  = null;
		// Port number to connect to
		private int    m_port       = 0;
		// Exception in the event a connection error occurs
		private IOException m_exception = null;

		// Connect by hostname string
		public SocketThread ( String host, int port)
		{
			// Assign to member variables
			m_host = host;
			m_port = port;
		}

		// Connect by inetAddress
		public SocketThread ( InetAddress inetAddr, int port )
		{
			// Assign to member variables
			m_inet = inetAddr;
			m_port = port;
		}

		public void run()
		{
			// temp socked var
			Socket sock = null;

			try
			{
				// string or an inet ?
				if (m_host != null)
				{
					// Connect - BLOCKING I/O
					sock = new Socket (m_host, m_port);
				}
				else
				{
					// Connect - BLOCKING I/O
					sock = new Socket (m_inet, m_port);
				}
			}
			catch (IOException ioe)
			{
				//System.out.println("IOexception:");
				m_exception = ioe;
				return;
			}


			// socket constructor returned without error,
			// connection finished
			m_connection = sock;
		}

		// connected?
		public boolean isConnected()
		{
			if (m_connection == null)
				return false;
			else
				return true;
		}

		// Error?
		public boolean isError()
		{
			if (m_exception == null)
				return false;
			else
				return true;
		}

		// disclose socket obtained to caller
		public Socket getSocket()
		{
			return m_connection;
		}

		// Get exception
		public IOException getException()
		{
			return m_exception;
		}
	}


}
