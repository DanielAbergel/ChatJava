import java.io.*; 
import java.net.*; 
import java.text.SimpleDateFormat; 
import java.util.*; 

/**
 * The server that can be run both as a console application or a GUI 
 */ 
public class Server { 
	// a unique ID for each connection 
	private static int uniqueId; 
	// an ArrayList to keep the list of the Client 
	private ArrayList<ClientThread> al; 
	// if I am in a GUI 
	private ServerGUI sg; 
	// to display time 
	private SimpleDateFormat sdf; 
	// the port number to listen for connection 
	private int port; 
	// the boolean that will be turned of to stop the server 
	private boolean keepGoing; 


	/**
	 *  server constructor that receive the port to listen to for connection as parameter 
	 *  in console 
	 *  @param port represent the server port number
	 */ 
	public Server(int port) { 
		this(port, null); 
	} 

	/**
	 *  server constructor that receive the port to listen to for connection as parameter 
	 *  in console 
	 *  @param port represent the server port number
	 *  @param sg represent the serverGUI
	 */ 
	public Server(int port, ServerGUI sg) { 
		// GUI or not 
		this.sg = sg; 
		// the port 
		this.port = port; 
		// to display hh:mm:ss 
		sdf = new SimpleDateFormat("HH:mm:ss"); 
		// ArrayList for the Client list 
		al = new ArrayList<ClientThread>(); 
	} 
	/**
	 * this function starts the server 
	 */
	public void start() { 
		keepGoing = true; 
		/* create socket server and wait for connection requests */ 
		try  
		{ 
			// the socket used by the server 
			ServerSocket serverSocket = new ServerSocket(port); 

			// infinite loop to wait for connections 
			while(keepGoing)  
			{ 
				// format message saying we are waiting 
				display("Server waiting for Clients on port " + port + "."); 

				Socket socket = serverSocket.accept();   // accept connection 
				// if I was asked to stop 
				if(!keepGoing) 
					break; 
				ClientThread t = new ClientThread(socket);  // make a thread of it 
				al.add(t);         // save it in the ArrayList 
				t.start(); 
			} 
			// I was asked to stop 
			try { 
				serverSocket.close(); 
				for(int i = 0; i < al.size(); ++i) { 
					ClientThread tc = al.get(i); 
					closing(tc);
				} 
			} 
			catch(Exception e) { 
				display("Exception closing the server and clients: " + e); 
			} 
		} 
		// something went bad 
		catch (IOException e) { 
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n"; 
			display(msg); 
		} 
	} 
	/**
	 * this function get a server and close the input,output and socket
	 * @param temp represents the current clientThread
	 */
	public void closing(ClientThread temp)
	{
		try { 
			temp.sInput.close(); 
			temp.sOutput.close(); 
			temp.socket.close(); 
		} 
		catch(IOException ioE) { 
			// not much I can do 
		} 
	}
	/*
	 * For the GUI to stop the server 
	 */   
	/*
	 * For the GUI to stop the server 
	 */ 
	protected void stop() { 
		keepGoing = false; 
		// connect to myself as Client to exit statement  
		// Socket socket = serverSocket.accept(); 
		try { 
			new Socket("localhost", port); 
		} 
		catch(Exception e) { 
			// nothing I can really do 
		} 
	} 
	/**
	 * Display an event (not a message) to the console or the GUI
	 * @param msg represents the message that we want to show on the screen 
	 */ 
	private void display(String msg) { 
		String time = sdf.format(new Date()) + " " + msg; 
		if(sg == null) 
			System.out.println(time); 
		else 
			sg.appendEvent(time + "\n"); 
	} 
	/**
	 * this function is for sending a private message from one client to another 
	 * with the format of $username-space-message 
	 * @param message  represent the message that we want to send
	 * @param UserName represent the client that want to sent the message to the other client
	 * @param Index represent the index of the other client
	 */
	private void sendPrivateMessage(String message , String UserName,int Index)
	{
		String time = sdf.format(new Date()); 
		String messageLf = time + " " + UserName + " : " + message + "\n"; 
		ClientThread CT = al.get(Index);
		CT.writeMsg(messageLf);
	}


	/**
	 * this function is for sending message from one client to all the other clients or for
	 * sending a private message with the format of $username-space-message 
	 * @param message  represent the message that we want to send
	 * @param UserName represent the client that want to sent a message
	 */ 
	private synchronized void broadcast(String message,String UserName) { 
		// add HH:mm:ss and \n to the message 
		String time = sdf.format(new Date()); 
		String messageLf = time + " " + UserName + " : " + message + "\n"; 
		// display message on console or GUI 
		int PrivateMessIndex = 0 ;

		if(message.charAt(0) == '$')
		{
			String[] PrivateMessage = message.split(" ", 2);
			PrivateMessage[0] = PrivateMessage[0].substring(1);
			for (int i = 0; i < al.size(); i++) {
				if(al.get(i).username.equals(PrivateMessage[0])) 
				{
					PrivateMessIndex = i;
					sendPrivateMessage(PrivateMessage[1],UserName,PrivateMessIndex);
				}
			}
		}
		else 
		{
			if(sg == null) 
				System.out.print(messageLf); 
			else 
				sg.appendRoom(messageLf);     // append in the room window 

			// we loop in reverse order in case we would have to remove a Client 
			// because it has disconnected 
			for(int i = al.size(); --i >= 0;) { 
				ClientThread ct = al.get(i); 
				// try to write to the Client if it fails remove it from the list 
				if(!ct.writeMsg(messageLf)) { 
					al.remove(i); 
					display("Disconnected Client " + ct.username + " removed from list."); 
				} 
			} 
		}
	} 

	// for a client who logoff using the LOGOUT message 
	synchronized void remove(int id) { 
		// scan the array list until we found the Id 
		for(int i = 0; i < al.size(); ++i) { 
			ClientThread ct = al.get(i); 
			// found it 
			if(ct.id == id) { 
				al.remove(i); 
				return; 
			} 
		} 
	} 

	/**
	 *  To run as a console application just open a console window and:  
	 * java Server 
	 * java Server portNumber 
	 * If the port number is not specified 1500 is used 
	 * @param args represent input
	 */  
	public static void main(String[] args) { 
		// start server on port 1500 unless a PortNumber is specified  
		int portNumber = 1500; 
		switch(args.length) { 
		case 1: 
			try { 
				portNumber = Integer.parseInt(args[0]); 
			} 
			catch(Exception e) { 
				System.out.println("Invalid port number."); 
				System.out.println("Usage is: > java Server [portNumber]"); 
				return; 
			} 
		case 0: 
			break; 
		default: 
			System.out.println("Usage is: > java Server [portNumber]"); 
			return; 

		} 
		// create a server object and start it 
		Server server = new Server(portNumber); 
		server.start(); 
	} 

	/** One instance of this thread will run for each client */ 
	class ClientThread extends Thread { 
		// the socket where to listen/talk 
		Socket socket; 
		ObjectInputStream sInput; 
		ObjectOutputStream sOutput; 
		// my unique id (easier for deconnection) 
		int id; 
		// the Username of the Client 
		String username; 
		// the only type of message a will receive 
		ChatMessage cm; 
		// the date I connect 
		String date; 

		/**
		 * Constructore of ClientThread
		 */  
		ClientThread(Socket socket) { 
			// a unique id 
			id = ++uniqueId; 
			this.socket = socket; 
			/* Creating both Data Stream */ 
			System.out.println("Thread trying to create Object Input/Output Streams"); 
			try 
			{ 
				// create output first 
				sOutput = new ObjectOutputStream(socket.getOutputStream()); 
				sInput  = new ObjectInputStream(socket.getInputStream()); 
				// read the username 
				username = (String) sInput.readObject(); 
				display(username + " just connected."); 
			} 
			catch (IOException e) { 
				display("Exception creating new Input/output Streams: " + e); 
				return; 
			} 
			// have to catch ClassNotFoundException 
			// but I read a String, I am sure it will work 
			catch (ClassNotFoundException e) { 
			} 
			date = new Date().toString() + "\n"; 
		} 

		// what will run forever 
		public void run() { 
			// to loop until LOGOUT 
			boolean keepGoing = true; 
			while(keepGoing) { 
				// read a String (which is an object) 
				try { 
					cm = (ChatMessage) sInput.readObject(); 
				} 
				catch (IOException e) { 
					display(username + " Exception reading Streams: " + e); 
					break;     
				} 
				catch(ClassNotFoundException e2) { 
					break; 
				} 
				// the messaage part of the ChatMessage 
				String message = cm.getMessage(); 

				// Switch on the type of message receive 
				switch(cm.getType()) { 

				case ChatMessage.MESSAGE: 
					broadcast(message,this.username); 
					break; 
				case ChatMessage.LOGOUT: 
					display(username + " disconnected with a LOGOUT message."); 
					keepGoing = false; 
					break; 
				case ChatMessage.WHOISIN: 
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n"); 
					// scan al the users connected 
					for(int i = 0; i < al.size(); ++i) { 
						ClientThread ct = al.get(i); 
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date); 
					} 
					break; 
				} 
			} 
			// remove myself from the arrayList containing the list of the 
			// connected Clients 
			remove(id); 
			close(); 
		} 

		/**
		 *try to close everything
		 */  
		private void close() { 
			// try to close the connection 
			try { 
				if(sOutput != null) sOutput.close(); 
			} 
			catch(Exception e) {} 
			try { 
				if(sInput != null) sInput.close(); 
			} 
			catch(Exception e) {}; 
			try { 
				if(socket != null) socket.close(); 
			} 
			catch (Exception e) {} 
		} 

		/**
		 * Write a String to the Client output stream
		 * @param msg represent the message that the server want to send to the clients 
		 */ 
		private boolean writeMsg(String msg) { 
			// if Client is still connected send the message to it 
			if(!socket.isConnected()) { 
				close(); 
				return false; 
			} 
			// write the message to the stream 
			try { 
				sOutput.writeObject(msg); 
			} 
			// if an error occurs, do not abort just inform the user 
			catch(IOException e) { 
				display("Error sending message to " + username); 
				display(e.toString()); 
			} 
			return true; 
		} 
	} 
}