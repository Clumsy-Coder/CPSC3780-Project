package server.backend;

import com.sun.istack.internal.NotNull;
import utilities.Message;
import utilities.MessageType;
import utilities.User;
import utilities.UserNetworkInfo;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Vector;


/**
 * Class used for serving as the middle man for sending and receiving messages.
 */
public class Server
{
	/**
	 * Server port number. Default is 5555
	 */
	private int serverPort = 5555;
	/**
	 * Flag indicating whether the server keeps running or stops
	 */
	private boolean         keepGoing;
	/**
	 * Contains all the messages received with SEND message type.
	 * Removes elements when client sends a GET request and adds
	 * it to the secondary message buffer GET_messageBuffer
	 */
	private Vector<Message> messageBuffer;
	/**
	 * Indicating the server as a user.
	 */
	private UserNetworkInfo serverUser;
	/**
	 * What port is the client is using. This changes everytime a message is received.
	 */
	private int             clientPort;
	
	//UDP
	/**
	 * UDP socket for the server
	 */
	private DatagramSocket udpSocket;
	/**
	 * Max packet size receivable. 1024 bytes is the default
	 */
	private final int MAX_INCOMING_SIZE = 1024;
	/**
	 * Contains all the clients connected to the server
	 */
	private Vector<UserNetworkInfo> connectedUsers;
	/**
	 * Secondary message buffer that contains messages from
	 * the first message buffer, only when the client sends
	 * a GET request. The secondary buffer is used for
	 * checking if the message is acknowledged. Remove the
	 * element when the ACK for the message is received.
	 */
	private Vector<Message>         GET_MessageBuffer;
	/**
	 * Contains the IP address for the message received from the client.
	 * Changes everytime a message is received.
	 */
	private InetAddress             clientIPaddress;
	/**
	 * Contains all the servers connected to this server
	 */
	private Vector<UserNetworkInfo> connectedServers;
	/**
	 * Inner class for listening for messages from clients/servers
	 */
	private ListenServer            serverListen;
	
	/**
	 * Initialize the server at port 5555
	 * @param username The username of the server
	 */
	Server(String username)
	{
		this(username, 5555);
	}//END DEFAULT CONSTRUCTOR Server(String)
	/**
	 * Initializes class attributes
	 *
	 * @param username   The username of the server
	 * @param serverPort Port number for the server to use
	 */
	Server(@NotNull String username, @NotNull int serverPort)
	{
		try
		{
			this.serverPort = serverPort;
			messageBuffer = new Vector<Message>();
			GET_MessageBuffer = new Vector<Message>();
			connectedUsers = new Vector<UserNetworkInfo>();
			connectedServers = new Vector<UserNetworkInfo>();
			
			serverUser = new UserNetworkInfo(Inet4Address.getLocalHost(),
			                                 serverPort,
			                                 new User(username));
			System.out.println(serverUser.getUser().getUsername() + " > IP address: " +
				                   serverUser.getIpAddress().getHostAddress() +
				                   " Server(String, int)");
		}//END TRY BLOCK
		
		catch (UnknownHostException e)
		{
//			e.printStackTrace();
			System.out.println(serverUser.getUser()
				                   .getUsername() + " > Unable to set the ip address Server(String, int)");
		}//END CATCH BLOCK UnknownHostException
	}//END CONSTRUCTOR Server(String, serverPort)
	/**
	 * Initializes the server, start the server and connects to another server.
	 * @param username           The username of the server
	 * @param serverPort         Port number for the server to use
	 * @param connectingServerIP IP address for the server to connect
	 * @param connectingPort     Port number for the server to connect
	 */
	Server(String username, int serverPort, String connectingServerIP, int connectingPort)
	{
		this(username, serverPort);
		this.startServer();
		this.connectServer(connectingServerIP, connectingPort);
	}
	/**
	 * Starts the server and listens for incoming messages.
	 */
	public void startServer()
	{
		//make a new UDP socket with the specified port number
		//while flag keepGoing is true
		//      check if keepGoing flag is false
		//          if true: close UDP socket and exit the method
		//      read the message
		//      check if message is not null
		//          if true:
		//              handle the message
		
		try
		{
			udpSocket = new DatagramSocket(serverPort);
			System.out.println("Server up and running on serverPort: " + Inet4Address.getLocalHost()
				.getHostAddress() + ":" + serverPort);
			keepGoing = true;
			
			//starting a new thread to listen for incoming messages
			serverListen = new ListenServer();
			serverListen.start();
		}//END TRY BLOCK
		
		catch (SocketException e)
		{
			System.out.println(serverUser.getUser().getUsername() + " > Unable to create DatagramSocket");
		}//END CATCH BLOCK SocketException
		
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}//END CATCH BLOCK UnknownHostException
	}//END METHOD startServer()
	/**
	 * Stops the server and stops listening for incoming messages.
	 */
	public void stopServer()
	{
		//check if there is any server connected
		for (UserNetworkInfo curServer : connectedServers)
		{
			Message disconnectMessage = new Message(MessageType.SERVER_DISCONNECT,
			                                        serverUser.getUser(),
			                                        curServer.getUser(),
			                                        null);
			
			this.sendServerMessage(disconnectMessage);
		}
		
		keepGoing = false;
		serverListen.stop();
		
		if (udpSocket != null)
		{
			udpSocket.close();
		}//END if (udpSocket != null)
	}//END METHOD stopServer()
	/**
	 * Used for sending user info to all connected clients.
	 * @param message Message to send.
	 */
	private synchronized void broadcastUser(@NotNull Message message)
	{
		//iterate through the connectedUsers
		//  make a message
		//      MessageType: message type
		//      source: server
		//      destination: current User in the vector
		//      payload: message payload.
		//  send message
		
		for (UserNetworkInfo curClient : connectedUsers)
		{
			Message broadcastMessage = new Message(message.getMessageType(),
			                                       serverUser.getUser(),
			                                       curClient.getUser(),
			                                       message.getPayload());
			this.sendMessage(broadcastMessage);
		}//END for (UserNetworkInfo curClient : connectedUsers)
	}//END METHOD broadcastUser(Message)
	/**
	 * Returns the UserNetworkInfo of the specified client.
	 * @param user The client being searched for
	 * @return UserNetworkInfo if found. NULL otherwise.
	 */
	private synchronized UserNetworkInfo getClient(@NotNull User user)
	{
		//iterate through the connectUsers
		//      check if username match
		//          if true:
		//              return the element
		//return null
		
		UserNetworkInfo connectedClient = null;
		
		for (int i = 0; i < connectedUsers.size(); i++)
		{
			if (connectedUsers.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				connectedClient = connectedUsers.get(i);
				return connectedClient;
			}//END if (connectedUsers.get(i).getUser().getUsername().equals(user.getUsername()))
		}//END for (int i = 0; i < connectedUsers.size(); i++)
		return connectedClient;
	}//END METHOD getClient(User)
	/**
	 * Gets the server info based on the parameter
	 * @param server The server to search
	 * @return UserNetworkInfo if found, NULL otherwise
	 */
	private synchronized UserNetworkInfo getServer(@NotNull User server)
	{
		for (UserNetworkInfo curServer : connectedServers)
		{
			if (curServer.getUser().getUsername().equals(server.getUsername()))
			{
				return curServer;
			}//END if(curServer.getUser().getUsername().equals(server.getUsername()))
		}//END for(UserNetworkInfo curServer : connectedServers)
		return null;
	}//END METHOD getServer(User)
	/**
	 * Used for removing a client from the list of connected users
	 * @param user Client to remove
	 */
	private synchronized void removeClient(@NotNull User user)
	{
		//iterate through the connectedUsers
		//      check if the username matches.
		//          if true
		//              remove the user.
		//              return and exit the method
		
		for (int i = 0; i < connectedUsers.size(); i++)
		{
			if (connectedUsers.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				connectedUsers.remove(i);
				return;
			}//END if (connectedUsers.get(i).getUser().getUsername().equals(user.getUsername()))
		}//END for (int i = 0; i < connectedUsers.size(); i++)
	}//END METHOD removeClient(User)
	/**
	 * Sending a message to a client as UDP packet.
	 * @param sendMessage The message to be sent.
	 */
	private synchronized void sendMessage(@NotNull Message sendMessage)
	{
		//code obtained online. It works, don't intend on understanding it.
		
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(sendMessage);
			byte[]          sendData          = byteArrayOutputStream.toByteArray();
			UserNetworkInfo destinationUser   = this.getClient(sendMessage.getDestination());
			UserNetworkInfo destinationServer = this.getServer(destinationUser.getServer());
			InetAddress     destinationIP     = null;
			int             destinationPort   = 0;
			
			//check if the destination is connected to this server.
			if (destinationUser.getServer().getUsername().equals(serverUser.getUser().getUsername()))
			{
				destinationIP = destinationUser.getIpAddress();
				destinationPort = destinationUser.getPort();
				
			}
			
			//if the destination is connected to another server
			else
			{
				destinationIP = destinationServer.getIpAddress();
				destinationPort = destinationServer.getPort();
				
			}
			
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               destinationIP,
			                                               destinationPort);
			
			udpSocket.send(sendPacket);
			oos.close();
			byteArrayOutputStream.close();
			
		}//END TRY BLOCK
		
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println(serverUser.getUser().getUsername() + " > Unable to write object or send packet");
			
		}//END CATCH BLOCK IOException
		
	}//END METHOD sendMessage(MessageType, User, User, Object)
	
	/**
	 * Sending a message to another server as a UDP packet
	 * @param message Message to be sent
	 */
	private synchronized void sendServerMessage(@NotNull Message message)
	{
		//code obtained online. It works, don't intend on understanding it.
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(message);
			byte[]      sendData        = byteArrayOutputStream.toByteArray();
			InetAddress destinationIP   = null;
			int         destinationPort = 0;
			
			//get the server IP address and port number
			for (UserNetworkInfo curServer : connectedServers)
			{
				if (curServer.getUser().getUsername().equals(message.getDestination().getUsername()))
				{
					destinationIP = curServer.getIpAddress();
					destinationPort = curServer.getPort();
					break;
				}//END if (curServer.getUser().getUsername().equals(message.getDestination().getUsername()))
			}//END for (UserNetworkInfo curServer : connectedServers)
			
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               destinationIP,
			                                               destinationPort);
			udpSocket.send(sendPacket);
			oos.close();
			byteArrayOutputStream.close();
		}//END TRY BLOCK
		
		catch (IOException e)
		{
			e.printStackTrace();
		}//END CATCH BLOCK FOR IOException
	}//END METHOD sendServerMessage(Message)
	/**
	 * Reading incoming messages.
	 * The method will hold until a packet is received.
	 * @return The message that was read.
	 */
	private Message readMessage()
	{
		//code obtained online. It works. Don't intend on understanding it.
		Message message = null;
		try
		{
			byte[]         incomingData   = new byte[MAX_INCOMING_SIZE];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			udpSocket.receive(incomingPacket);
			byte[]               data        = incomingPacket.getData();
			ByteArrayInputStream in          = new ByteArrayInputStream(data);
			ObjectInputStream    inputStream = new ObjectInputStream(in);
			//read the object
			message = (Message) inputStream.readObject();
			clientIPaddress = incomingPacket.getAddress();
			this.clientPort = incomingPacket.getPort();
		}//END TRY BLOCK
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println(serverUser.getUser().getUsername() + " > unable to receive packet or read object");
		}//END CATCH BLOCK IOException
		
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}//END CATCH BLOCK ClassNotFoundException
		return message;
	}//END METHOD readMessage()
	/**
	 * Sends connected Users to the server
	 * Sends all the connected clients on this server to a server
	 * @param message Message being sent. Must contain destination
	 */
	private synchronized void broadcastUserList(@NotNull Message message)
	{
		//iterate through the connectedUsers
		//  make a message
		//      MessageType: message type
		//      source: server
		//      destination: current User in the vector
		//      payload: message payload.
		//  send message
		
		//iterate through the connectedUsers
		//  set the payload to the current connectUser
		//  send the message to server
		for (UserNetworkInfo curClient : connectedUsers)
		{
			message.setPayload(curClient);
			this.sendServerMessage(message);
		}
	}//END METHOD broadcasrUserList(Message)
	
	/**
	 * Connect to another server.
	 * Sends a CONNECT_SERVER message to the destination.
	 * Waits for a ACK_SERVER_CONNECT message to complete the
	 * server connection.
	 * @param ipAddress The IP address to send to
	 * @param port      Port number of the destination server
	 */
	public synchronized void connectServer(@NotNull String ipAddress, @NotNull int port)
	{
		//make a message
		//  Type: SERVER_CONNECT
		//  Source: server
		//  Destination: connecting server (server being connected to)
		//  payload: server (info about this server)
		//create a UserNetworkInfo
		//  IP address: ip address (ip address for connecting to the server)
		//  port: port (port number for the connecting to the server)
		//  user: null (will be set later once a ACK_SEVER_CONNECT is received.)
		//add UserNetworkInfo to connectedServer
		//send the message to the server.
		try
		{
			InetAddress serverIpAddress = Inet4Address.getByName(ipAddress);
			System.out.println("IP address: " + serverIpAddress.getHostAddress());
			UserNetworkInfo newServer = new UserNetworkInfo(serverIpAddress, port, null);
			connectedServers.add(newServer);
			Message message = new Message(MessageType.SERVER_CONNECT,
			                              serverUser.getUser(),
			                              null,
			                              serverUser);
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(message);
			byte[] sendData = byteArrayOutputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               serverIpAddress,
			                                               port);
			udpSocket.send(sendPacket);
			oos.close();
			byteArrayOutputStream.close();
		}//END TRY BLOCK
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}//END CATCH BLOCK UnknownHostException
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println(serverUser.getUser().getUsername() +
				                   " > Unable to write object or send packet: connectServer(String, int)");
		}//END CATCH BLOCK IOException
	}//END METHOD connectServer(String, int)
	/**
	 * Handles a SEND message from a client.
	 * The server checks if the destination is connected to this server, if true
	 * , then store it in the message buffer, otherwise send it to the
	 * server that has the client connected to it (directly connected)
	 * @param message The message to process
	 */
	private final synchronized void handleSEND_message(@NotNull Message message)
	{
		//get the message and store it in messageBuffer, IF the destination is connected to this server.
		//  else send it to the server that the client is connected to it.
		//message format received:
		//  Type: SEND
		//  source: source (the sender)
		//  destination: destination (the receiver)
		//  payload: text message
		//  NOTE: sequence number must be embedded and NOT null
		
		/*  ex: server config
				S1 - S2 - S3
			    S1 wants to send a message to S3
			        S1 sends the message to S2
			        S2 checks if the message is destined for this server
			            if true: handle it
			            else:
			                check which server to send it to
			                send it. (in this case send it to S3)
			                    The destination IP changes.
		*/
		System.out.println("------------------------------------------------------------");
		System.out.println(serverUser.getUser().getUsername() + " > Message from: '" + message.getSource()
			.getUsername() + "'");
		System.out.println("\t\tTo: " + message.getDestination().getUsername());
		System.out.println("\t\tContent: " + message.getPayload().toString());
		System.out.println("\t\tSequence number: " + message.getSequenceNumber());
		System.out.println("\t\tDestination Server: " + this.getClient(message.getDestination())
			.getServer()
			.getUsername());
		boolean thisServer = false;
		
		UserNetworkInfo destination = this.getClient(message.getDestination());
		if (destination.getServer().getUsername().equals(serverUser.getUser().getUsername()))
		{
			thisServer = true;
		}//END if (destination.getServer().getUsername().equals(serverUser.getUser().getUsername()))
		if (thisServer)
		{
			messageBuffer.add(message);
		}//END if (thisServer)
		else
		{
			System.out.println(serverUser.getUser().getUsername() + " > Forwarding message");
			this.sendMessage(message);
		}//END ELSE FOR if (thisServer)
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleSEND_message(Message)
	/**
	 * Handles a GET message from a client that is attempting to retrieve messages that is
	 * destined to it.
	 * @param message The message to process
	 */
	private final synchronized void handleGET_message(@NotNull Message message)
	{
		//message format received:
		//  Type: GET
		//  source: source (the sender)
		//  destination: server
		//  payload: null
		
		//get the message that is destined to client B
		//remove it from messageBuffer and place it in GET_MessageBuffer
		//send the messages destined to client B from GET_MessageBuffer

//		System.out.println("------------------------------------------------------------");
//		System.out.println(serverUser.getUser().getUsername() + " > GET from: '" + message.getSource()
//			.getUsername() + "'");
		
		//using a new thread in case there's multiple messages that needs to be sent
		Runnable tempThread = () ->
		{
			for (int i = 0; i < messageBuffer.size(); i++)
			{
				//get the message that is destined to client B
				if (messageBuffer.get(i)
					.getDestination()
					.getUsername()
					.equals(message.getSource().getUsername()))
				{
					GET_MessageBuffer.add(messageBuffer.remove(i));
				}//END IF messageBuffer username at i is equal to message
			}//END for (int i = 0; i < messageBuffer.size(); i++)
			//send each message to Client B
			for (int i = 0; i < GET_MessageBuffer.size(); i++)
			{
				//message format sent
				//  Type: GET
				//  source: source (the one who the sent the SEND message)
				//  destination: destination (the one who sent the GET message)
				//  payload: text message
				//  NOTE: sequence number is embedded.
				Message getMessage = GET_MessageBuffer.get(i);
				getMessage.setMessageType(MessageType.GET);
				this.sendMessage(getMessage);
//				System.out.println(serverUser.getUser().getUsername() + " > GET request sent");
			}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
		};//END Lambda Runnable FUNCTION
		if (messageBuffer.size() > 0)
		{
			new Thread(tempThread).start();
			
		}//END if(messageBuffer.size() > 0)
//		System.out.println("------------------------------------------------------------");
	}//END METHOD handleGET_message(Message)
	/**
	 * Handles messages that have ACK message type
	 * Handles a ACK message from a client that is acknowledging a message.
	 * @param message The message to process
	 */
	private final synchronized void handleACK_message(@NotNull Message message)
	{
		//message format received
		//  Type: ACK
		//  source: source (the sender)
		//  destination: destination (who is the sender sending to)
		//  payload: sequence number
		
		//check which message is it being acknowledged for. sequence number, source and destination
		//foreword the ACK message to it's destination
		//remove message from GET_requestBuffer
		System.out.println("------------------------------------------------------------");
		System.out.println(serverUser.getUser().getUsername() + " > ACK from: '" + message.getSource()
			.getUsername() + "'");
		System.out.println("\t\tTo: " + message.getDestination().getUsername());
		BigInteger bigInt = (BigInteger) message.getPayload();
		System.out.println("\t\tSequence number: " + (bigInt != null ?
		                                              bigInt.toString() :
		                                              null));
		BigInteger sequenceNum = (BigInteger) message.getPayload();
		for (int i = 0; i < GET_MessageBuffer.size(); i++)
		{
			//message format sent
			//  Type: ACK
			//  source: source (the one who sent ACK message)
			//  destination: destination (the one who sent the SEND message)
			//  payload: sequence number
			
			//check for the sequence number, source and destination
			if (GET_MessageBuffer.get(i).getSequenceNumber().compareTo(sequenceNum) == 0 &&
				GET_MessageBuffer.get(i).getSource().getUsername().equals(message.getDestination().getUsername()) &&
				GET_MessageBuffer.get(i).getDestination().getUsername().equals(message.getSource().getUsername()))
			{
				Message ackMessage = GET_MessageBuffer.remove(i);
				
				this.sendMessage(message);
				System.out.println("\n" + serverUser.getUser().getUsername() + " > ACK sent");
				System.out.println("\t\tGET_MessageBuffer.size() : " + GET_MessageBuffer.size());
			}//END IF FOR checking the sequence number, source and destination match properly
			//in the GET_messageBuffer
		}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleACK_message(Message)
	/**
	 * Handles messages that have USERS message type
	 * Handles a USERS message from a server which telling which client has connected to the server
	 * (NOT this server)
	 * @param message The message to process
	 */
	private final synchronized void handleUSERS_message(@NotNull Message message)
	{
		System.out.println("------------------------------------------------------------");
		//message format received:
		//  Type: USERS
		//  source: server
		//  destination: user (who are connected)
		//  payload: User object (must contain initial sequence num)
		//  NOTE: must contain sequence number.
		
		//iterate through the connectedUser vector
		//  check if the user exists
		//      if true:
		//          set userExists to true
		//          break out of the loop
		//check if it's a new user
		//  if true:
		//      add the user to the connectedUser list.
		//
		//let every client know who just connected to the server.
		//
		//let every server connected know who just connected to the server
		System.out.println(serverUser.getUser().getUsername() + " > USERS message received");
		System.out.println("\t\tSource: " + message.getSource().getUsername());
		System.out.println("\t\tPayload:");
		boolean userExists = false;
		UserNetworkInfo newUser = (UserNetworkInfo) message.getPayload();
		System.out.println("\t\t\tUsername: " + newUser.getUser().getUsername());
		System.out.println("\t\t\tserver: " + newUser.getServer().getUsername());
		for (UserNetworkInfo curUser : connectedUsers)
		{
			if (curUser.getUser().getUsername().equals(newUser.getUser().getUsername()))
			{
				System.out.println(serverUser.getUser().getUsername() + " > User already exists");
				userExists = true;
				return;
			}//END if (curUser.getUser().getUsername().equals(newUser.getUser().getUsername()))
		}//END for (UserNetworkInfo curUser : connectedUsers)
		//if it's a new user.
		UserNetworkInfo server = this.getServer(newUser.getServer());
		if (server == null)
		{
			newUser.setServer(message.getSource());
		}//END if (server == null)
		connectedUsers.add(newUser);
		System.out.println(serverUser.getUser().getUsername() + " > User added");
		//tell all clients connected to the server
		if (connectedUsers.size() >= 2)
		{
			for (UserNetworkInfo curClient : connectedUsers)
			{
				if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
				{
					Message newClientMessage = new Message(MessageType.USERS,
					                                       serverUser.getUser(),
					                                       curClient.getUser(),
					                                       newUser.getUser());
					this.sendMessage(newClientMessage);
				}//END if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
			}//END for (UserNetworkInfo curClient : connectedUsers)
		}//END if (connectedUsers.size() >= 2)
		if (connectedServers.size() > 0)
		{
			for (UserNetworkInfo curServer : connectedServers)
			{
				//to prevent sending the info back to the source
				if (curServer.getUser().getUsername().equals(message.getSource().getUsername()))
				{
					continue;
				}//END if (curServer.getUser().getUsername().equals(message.getSource().getUsername()))
				Message newClientMessage = new Message(MessageType.USERS,
				                                       serverUser.getUser(),
				                                       curServer.getUser(),
				                                       newUser);
				this.sendServerMessage(newClientMessage);
			}//END for (UserNetworkInfo curServer : connectedServers)
		}//END if (connectedServers.size() > 0)
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleUSERS_message(Message)
	/**
	 * Handles a CONNECT message from a client that is attempting to connect to this server
	 * @param message The message to process
	 */
	private final synchronized void handleCONNECT_message(Message message)
	{
		//message format received
		//  Type: CONNECT
		//  source: source (the one who is connecting)
		//  destination: server
		//  payload: User object
		//  NOTE: must contain sequence number
		
		//tell the newly connected user what users are currently connected:
		//  iterate through the connectedUsers
		//      send a message of the new user about the current connected users.
		//          MessageType: USERS
		//          source: server
		//          destination: new User
		//          payload: current User from the vector
		//              NOTE: must include the sequence number.
		//tell everyone connected (not the new user) who just connected:
		//  iterate through the connectedUsers
		//      send a message to the current User in the vector
		//          MessageType: USERS:
		//          source: server
		//          destination: current User
		//          payload: new User
		//add new User to connectedUser vector
		//tell other servers about who just connected
		
		System.out.println("------------------------------------------------------------");
		//get the client and add it to the connectedUser vector
		User            user         = message.getSource();
		UserNetworkInfo clientIPInfo = new UserNetworkInfo(clientIPaddress, clientPort, user);
		System.out.println(serverUser.getUser()
			                   .getUsername() + " > User: '" + user.getUsername() + "' is now CONNECTED");
		System.out.println("\t\tsequence number: " + user.getSequenceNumber());
		for (UserNetworkInfo connectedUser : connectedUsers)
		{
			Message broadcastMessage = new Message(MessageType.USERS,
			                                       serverUser.getUser(),
			                                       message.getSource(),
			                                       connectedUser.getUser());
			try
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream    oos  = new ObjectOutputStream(baos);
				oos.writeObject(broadcastMessage);
				byte[] sendData = baos.toByteArray();
				
				InetAddress destinationIP   = clientIPInfo.getIpAddress();
				int         destinationPort = clientIPInfo.getPort();
				
				DatagramPacket sendPacket = new DatagramPacket(sendData,
				                                               sendData.length,
				                                               destinationIP,
				                                               destinationPort);
				udpSocket.send(sendPacket);
				oos.close();
			}//END TRY BLOCK
			catch (IOException e)
			{
				
			}//END CATCH BLOCK IOException
		}//END for (UserNetworkInfo connectedUser : connectedUsers)
		//let everyone connected to the server know that a new user has connected to the server.
		//connectedUsers.size() + 1 because you don't want to broadcast to the original sender.
		//  the original sender would think that's a new user.
		if (connectedUsers.size() + 1 >= 2)
		{
			System.out.println(serverUser.getUser().getUsername() +
				                   " > sending info to all connected users about new user. ");
			//message format sent
			//  Type: USERS
			//  source: server
			//  destination: all users/servers connected
			//  payload: User object
			//  NOTE: must contain sequence number
			
			//send the message to the clients connected to this server
			for (UserNetworkInfo curClient : connectedUsers)
			{
				if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
				{
					Message newClientMessage = new Message(MessageType.USERS,
					                                       serverUser.getUser(),
					                                       curClient.getUser(),
					                                       message.getSource());
					this.sendMessage(newClientMessage);
				}//END if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
			}//END for (UserNetworkInfo curClient : connectedUsers)
		}//END if (connectedUsers.size() + 1 >= 2)
		clientIPInfo.setServer(serverUser.getUser());
		for (UserNetworkInfo curServer : connectedServers)
		{
			//tell other severs who just connected
			Message serverMsg = new Message(MessageType.USERS,
			                                serverUser.getUser(),
			                                curServer.getUser(),
			                                clientIPInfo);
			this.sendServerMessage(serverMsg);
		}//END for (UserNetworkInfo curServer : connectedServers)
		connectedUsers.add(clientIPInfo);
		System.out.println("\t\tconnected clients: " + connectedUsers.size());
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleCONNECT_message(Message)
	/**
	 * Handles a DISCONNECT message from a client which is disconnecting from this server
	 * @param message The message to process
	 */
	private final synchronized void handleDISCONNECT_message(Message message)
	{
		//message format received
		//  Type: DISCONNECT
		//  source: source (the sender)
		//  destination: server
		//  payload: null
		
		//remove the user from connectedUser vector
		//tell everyone connected about who just disconnected:
		//  check if they're 1 or more users connected to the server.
		//      if true:
		//          create a new Message
		//              MessageType: DISCONNECT
		//              source: source
		//              destination: null (will be set later on)
		//              payload: the disconnected user
		//          broadcast the message
		//tell other servers about who just disconnected
		System.out.println("------------------------------------------------------------");
		//get the client and remove it from the connectedUser vector
		User            user            = message.getSource();
		UserNetworkInfo userNetworkInfo = this.getClient(user);
		this.removeClient(user);
		System.out.println(serverUser.getUser()
			                   .getUsername() + " > User: '" + user.getUsername() + "' is now DISCONNECTED");
		//broadcast the connectUser vector to all connected users/server
		//let everyone know who just disconnected
		if (connectedUsers.size() >= 1)
		{
			//message format sent
			//  Type: DISCONNECT
			//  source: server
			//  destination: all users/servers connected
			//  payload: User object
			for (UserNetworkInfo curClient : connectedUsers)
			{
				//send the message to clients connected to this server. NOT from other servers
				if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
				{
					Message disconnectMessage = new Message(MessageType.DISCONNECT,
					                                        message.getSource(),
					                                        curClient.getUser(),
					                                        message.getSource());
					this.sendMessage(disconnectMessage);
				}//END if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
			}//END for (UserNetworkInfo curClient : connectedUsers)
			for (UserNetworkInfo curServer : connectedServers)
			{
				if (userNetworkInfo.getServer().getUsername().equals(curServer.getUser().getUsername()))
				{
					continue;
				}//END if (userNetworkInfo.getServer().getUsername().equals(curServer.getUser().getUsername()))
				Message disconnectMessage = new Message(MessageType.DISCONNECT,
				                                        message.getSource(),
				                                        curServer.getUser(),
				                                        message.getSource());
				this.sendServerMessage(disconnectMessage);
			}//END for (UserNetworkInfo curServer : connectedServers)
		}//END if (connectedUsers.size() >= 1)
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleDISCONNECT_message(Message)
	
	/**
	 * Handles a CONNECT_SERVER message from a server which is attempting to connect
	 * @param message The message to process
	 */
	private final synchronized void handleSERVER_CONNECT_message(Message message)
	{
		//create a new UserNetworkInfo
		//  ipAddress: ipAddress
		//  port: port
		//  user: user
		//send ACK_SERVER_CONNECT back to the server
		//tell the new server who is currently connected to this server (not new server)
		System.out.println("------------------------------------------------------------");
		UserNetworkInfo newServer = (UserNetworkInfo) message.getPayload();
		connectedServers.add(newServer);
		System.out.println(serverUser.getUser().getUsername() + " > SERVER_CONNECT message received");
		System.out.println("\t\tSource: " + message.getSource().getUsername());
		System.out.println("\t\tIP address: " + newServer.getIpAddress().getHostAddress());
		System.out.println("\t\tPort: " + newServer.getPort());
		
		Message ackMessage = new Message(MessageType.ACK_SERVER_CONNECT,
		                                 serverUser.getUser(),
		                                 newServer.getUser(),
		                                 serverUser);
		System.out.println(serverUser.getUser().getUsername() + " > Sending ACK_SERVER_CONNECT to: ");
		System.out.println("\t\tServer: " + ackMessage.getDestination().getUsername());
		System.out.println("\t\tIP address: " + newServer.getIpAddress().getHostAddress());
		System.out.println("\t\tPort: " + newServer.getPort());
		this.sendServerMessage(ackMessage);
		System.out.println(serverUser.getUser().getUsername() + " > Sending client info");
		//tell the new server who is connected to this server
		Message userMessage = new Message(MessageType.USERS,
		                                  serverUser.getUser(),
		                                  newServer.getUser(),
		                                  null);
		Runnable userThread = () ->
		{
			this.broadcastUserList(userMessage);
		};
		new Thread(userThread).start();
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleSERVER_CONNECT_message(Message)
	
	/**
	 * Handles a SERVER_DISCONNECT message from a server who is disconnecting this server
	 * @param message The message to process
	 */
	private final synchronized void handleSERVER_DISCONNECT(Message message)
	{
		//find the server disconnecting
		//  remove it from the connectedServer vector
		System.out.println("------------------------------------------------------------");
		System.out.println(serverUser.getUser().getUsername() + " > SERVER_DISCONNECT message received");
		System.out.println("\t\tSource: " + message.getSource().getUsername());
		for (int i = 0; i < connectedServers.size(); i++)
		{
			if (connectedServers.get(i).getUser().getUsername().equals(message.getSource().getUsername()))
			{
				connectedServers.remove(i);
				break;
			}//END if (connectedServers.get(i).getUser().getUsername().equals(message.getSource().getUsername()))
		}//END for (int i = 0; i < connectedServers.size(); i++)
		System.out.println(serverUser.getUser().getUsername() + " > Server: " + message.getSource().getUsername() +
			                   " disconnected");
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleSERVER_DISCONNECT(Message)
	
	/**
	 * Handles a ACK message from a server who is connecting to this server
	 * @param message The message to process
	 */
	private final synchronized void handleACK_SERVER_CONNECT_message(Message message)
	{
		//message content
		//  Type: ACK_SERVER_CONNECT
		//  source: source
		//  destination : destination
		//  payload: User (contains info about the server)
		
		//find which server is being acknowledged from connectedServers
		//set the user from the message
		//tell the server which clients are connected to this server
		System.out.println("------------------------------------------------------------");
		UserNetworkInfo ackServer = (UserNetworkInfo) message.getPayload();
		System.out.println(serverUser.getUser().getUsername() + " > ACK_SERVER_CONNECT message received");
		System.out.println("\t\tSource: " + message.getSource().getUsername());
		System.out.println("\t\tIP address: " + ackServer.getIpAddress().getHostAddress());
		System.out.println("\t\tPort: " + ackServer.getPort());
		System.out.println(serverUser.getUser().getUsername() + " > setting server");
		for (UserNetworkInfo curServer : connectedServers)
		{
			if (curServer.getIpAddress().getHostAddress().equals(ackServer.getIpAddress().getHostAddress()) &&
				curServer.getPort() == ackServer.getPort())
			{
				System.out.println(serverUser.getUser().getUsername() + " > User object set");
				curServer.setUser(ackServer.getUser());
			}//END if (curServer.getIpAddress().getHostAddress().equals(ackServer.getIpAddress().getHostAddress()) &&
			 //        curServer.getPort() == ackServer.getPort())
		}//END for (UserNetworkInfo curServer : connectedServers)
		System.out.println(serverUser.getUser().getUsername() + " > Sending client info. " +
			                   "connectUsers.size() : " + connectedUsers.size());
		User destination = ackServer.getUser();
		Message userMessage = new Message(MessageType.USERS,
		                                  serverUser.getUser(),
		                                  destination,
		                                  null);
		Runnable userThread = () ->
		{
			this.broadcastUserList(userMessage);
		};
		new Thread(userThread).start();
		System.out.println("------------------------------------------------------------");
	}//END METHOD handleACK_SERVER_CONNECT_message(Message)
	/**
	 * Receives the message and calls the appropriate method to handle the message
	 * @param message Message received.
	 */
	private synchronized void handleMessage(Message message)
	{
		//handle the message based on the type.
		//possible type:
		//  SEND
		//      store the message in messageBuffer
		//  GET
		//      get the message for client, remove it from messageBuffer, add it to GET_messageRequest,
		//          send the message to the client.
		//  ACK
		//      find the message being acknowledged, remove it from the
		//          GET_messageBuffer and send the ACK to the appropiate client.
		//  USERS
		//  CONNECT
		//      get the username and IP address and add it to the connectedUsers vector.
		//      send the updated user list to everyone connected.
		//  DISCONNECT
		//      get the username and remove it from connectedUsers vector.
		//      send an update to the rest of the clients who are connected.
		switch (message.getMessageType())
		{
			case SEND:
			{
				this.handleSEND_message(message);
				break;
			}//END CASE SEND
			
			case GET:
			{
				this.handleGET_message(message);
				break;
			}//END CASE GET
			
			case ACK:
			{
				this.handleACK_message(message);
				break;
			}//END CASE ACK
			
			case USERS:
			{
				this.handleUSERS_message(message);
				break;
			}//END CASE USERS
			
			case CONNECT:
			{
				this.handleCONNECT_message(message);
				break;
			}//END CASE CONNECT
			
			case DISCONNECT:
			{
				this.handleDISCONNECT_message(message);
				break;
			}//END CASE DISCONNECT
			
			case SERVER_CONNECT:
			{
				this.handleSERVER_CONNECT_message(message);
				break;
			}//END CASE SERVER_CONNECT
			
			case ACK_SERVER_CONNECT:
			{
				this.handleACK_SERVER_CONNECT_message(message);
				break;
			}//END CASE ACK_SERVER_CONNECT
			
			case SERVER_DISCONNECT:
			{
				this.handleSERVER_DISCONNECT(message);
				break;
			}//END CASE SERVER_DISCONNECT
			
			default:
			{
				System.out.println(serverUser.getUser()
					                   .getUsername() + " > unknown message type from: " + message.getSource()
					.getUsername());
				break;
			}//END CASE default
		}//END switch(message.getMessageType)
	}//END METHOD handleMessage(Message)
	/**
	 * Display information about servers connected to this server
	 */
	public void printServers()
	{
		System.out.println("------------------------------------------------------------");
		if (connectedServers.size() > 0)
		{
			for (UserNetworkInfo curServer : connectedServers)
			{
				System.out.println("Server name: " + curServer.getUser().getUsername());
				System.out.println("\t\tIP address: " + curServer.getIpAddress().getHostAddress());
				System.out.println("\t\tPort: " + curServer.getPort());
			}//END for (UserNetworkInfo curServer : connectedServers)
		}//END if (connectedServers.size() > 0)
		else
		{
			System.out.println("No servers connected.");
		}//END ELSE FOR if (connectedServers.size() > 0)
		System.out.println("------------------------------------------------------------");
	}//END METHOD printServers()
	/**
	 * Display information about clients connected to the server.
	 */
	public void printClients()
	{
		System.out.println("------------------------------------------------------------");
		if (connectedUsers.size() > 0)
		{
			for (UserNetworkInfo curClient : connectedUsers)
			{
				System.out.println("Client name: " + curClient.getUser().getUsername());
				System.out.println("\t\tServer: " + curClient.getServer().getUsername());
				if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
				{
					System.out.println("\t\tServer IP: " + serverUser.getIpAddress().getHostAddress());
					System.out.println("\t\tServer port: " + serverUser.getPort());
				}//END if (curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
				
				else if (this.getServer(curClient.getServer()) != null)
				{
					UserNetworkInfo serverInfo = this.getServer(curClient.getServer());
					System.out.println("\t\tServer IP: " + serverInfo.getIpAddress().getHostAddress());
					System.out.println("\t\tServer port: " + serverInfo.getPort());
				}//END else if (this.getServer(curClient.getServer()) != null)
			}//END for (UserNetworkInfo curClient : connectedUsers)
		}//END if (connectedUsers.size() > 0)
		else
		{
			System.out.println("No clients connected. ");
		}//END if (connectedUsers.size() > 0)
		System.out.println("------------------------------------------------------------");
	}//END METHOD printClients()
	/**
	 * Display information about this server.
	 */
	public void whoami()
	{
		System.out.println("------------------------------------------------------------");
		System.out.println("Server name: " + serverUser.getUser().getUsername());
		System.out.println("IP address: " + serverUser.getIpAddress().getHostAddress());
		System.out.println("Port: " + serverUser.getPort());
		System.out.println("------------------------------------------------------------");
	}//END METHOD whoami()
	/**
	 * Inner class used for listening incoming messages. Runs on a seperate thread.
	 */
	private class ListenServer extends Thread
	{
		public void run()
		{
			while (keepGoing)
			{
				if (!keepGoing)
				{
					udpSocket.close();
					System.out.println(serverUser.getUser().getUsername() + " > Server stopped.");
					return;
				}//END if(!keepGoing)
				Message message = readMessage();
				//handle the message if the Message object is not null
				if (message != null)
				{
					handleMessage(message);
				}//END if(message != null)
			}//END while(keepGoing)
		}//END METHOD run()
	}//END CLASS ListenServer
}//END CLASS Server
