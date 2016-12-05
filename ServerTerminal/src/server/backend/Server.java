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
	private UserNetworkInfo            serverUser;
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
	
	private Vector<UserNetworkInfo> connectedServers;
	
	private ListenServer serverListen;
	
	/**
	 * Server starts in serverPort 5555
	 * @param username The username of the server
	 */
	Server(String username)
	{
		this(username, 5555);
		
	}//END DEFAULT CONSTRUCTOR Server(String)
	
	/**
	 * Server starts on specified serverPort
	 * @param username The username of the server
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
//			InetAddress ipAddress = InetAddress.getLocalHost();
			serverUser = new UserNetworkInfo(Inet4Address.getLocalHost(),
			                                 serverPort,
			                                 new User(username));
			System.out.println(serverUser.getUser().getUsername() + " > IP address: " +
				                   serverUser.getIpAddress().getHostAddress() +
				                   " Server(String, int)");
		}
		
		catch (UnknownHostException e)
		{
//			e.printStackTrace();
			System.out.println(serverUser.getUser().getUsername() + " > Unable to set the ip address Server(String, int)");
		}
		
	}//END CONSTRUCTOR Server(String, serverPort)
	
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
//			while (keepGoing)
//			{
//				if (!keepGoing)
//				{
//					udpSocket.close();
//					System.out.println(serverUser.getUser().getUsername() + " > Server stopped.");
//					return;
//				}//END if(!keepGoing)
//
//				Message message = this.readMessage();
//				//handle the message if the Message object is not null
//				if (message != null)
//				{
//					this.handleMessage(message);
//
//				}//END if(message != null)
//
//			}//END while(keepGoing)
			
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
		for(UserNetworkInfo curServer : connectedServers)
		{
			Message disconnectMessage = new Message(MessageType.SERVER_DISCONNECT,
			                                        serverUser.getUser(),
			                                        curServer.getUser(),
			                                        null);
			
			this.sendServerMessage(disconnectMessage);
		}
		
		keepGoing = false;
		serverListen.stop();
		
		if(udpSocket != null)
		{
			udpSocket.close();
		}
		
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
	 * Returns the user network info of the specified user.
	 * @param user The user being searched for
	 * @return UserNetworkInfo if found. null otherwise.
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
	 * Used for sending a message to a client as UDP packet.
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
			byte[] sendData = byteArrayOutputStream.toByteArray();
			UserNetworkInfo destinationUser = this.getClient(sendMessage.getDestination());
			InetAddress destinationIP = null;
			int         destinationPort = 0;
			
			//todo check the new send message is working. NOTE: possible null values
			//check if the destination is connected to this server.
			if(destinationUser.getServer().getUsername().equals(serverUser.getUser().getUsername()))
			{
				destinationIP = destinationUser.getIpAddress();
				destinationPort = destinationUser.getPort();
				
			}
			
			//if the destination is connected to another server
			else
			{
				for(UserNetworkInfo curServer : connectedServers)
				{
//					if(curServer.getServer().getUsername().equals(destinationUser.getServer().getUsername()))
					if(destinationUser.getServer().getUsername().equals(curServer.getUser().getUsername()))
					{
						destinationIP = curServer.getIpAddress();
						destinationPort = curServer.getPort();
						break;
					}
				}
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
	
	private synchronized void sendServerMessage(@NotNull Message message)
	{
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(message);
			byte[] sendData = byteArrayOutputStream.toByteArray();
//			UserNetworkInfo destinationUser = this.getClient(message.getDestination());
			InetAddress destinationIP = null;
			int         destinationPort = 0;
			
			for( UserNetworkInfo curServer : connectedServers)
			{
				if(curServer.getUser().getUsername().equals(message.getDestination().getUsername()))
				{
					destinationIP = curServer.getIpAddress();
					destinationPort = curServer.getPort();
				}
			}
			
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               destinationIP,
			                                               destinationPort);
			
			udpSocket.send(sendPacket);
			oos.close();
			byteArrayOutputStream.close();
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
	}//END METHOD sendServerMessage(Message)
	
	/**
	 * Used for reading incoming messages.
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
			
		}
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
	 * @param message Message being sent. Must contain destination
	 */
	//todo implement broadcastUserList method
	private synchronized void broadcastUserList(@NotNull Message message)
	{
		/*
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
			                                       serverUser,
			                                       curClient.getUser(),
			                                       message.getPayload());
			this.sendMessage(broadcastMessage);
			
		}//END for (UserNetworkInfo curClient : connectedUsers)
		 */
		
		//iterate through the connectedUsers
		//  set the payload to the current connectUser
		//  send the message to server
		
		for(UserNetworkInfo curClient : connectedUsers)
		{
			
			message.setPayload(curClient);
			this.sendServerMessage(message);
		}
		
	}//END METHOD broadcasrUserList(Message)
	
	//todo implement connectServer(String, int)
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
	 * Handles messages that have SEND message type
	 * @param message Message to handle
	 */
	private final synchronized void handleSEND_message(@NotNull Message message)
	{
		//get the message and store it in messageBuffer
		//message format received:
		//  Type: SEND
		//  source: source (the sender)
		//  destination: destination (the receiver)
		//  payload: text message
		//  NOTE: sequence number must be embedded and NOT null
		
		System.out.println("------------------------------------------------------------");
//		messageBuffer.add(message);
		System.out.println(serverUser.getUser().getUsername() + " > Message from: '" + message.getSource()
			.getUsername() + "'");
		System.out.println("\t\tTo: " + message.getDestination().getUsername());
		System.out.println("\t\tContent: " + message.getPayload().toString());
		System.out.println("\t\tSequence number: " + message.getSequenceNumber());
		
		boolean thisServer = false;
//		for(UserNetworkInfo curClient : connectedUsers)
//		{
//			if(curClient.getUser().getUsername().equals(message.getDestination().getUsername()))
//			{
//				thisServer = true;
//				break;
//			}
//		}
		
		UserNetworkInfo destination = this.getClient(message.getDestination());
		if(destination.getServer().getUsername().equals(serverUser.getUser().getUsername()))
		{
			thisServer = true;
		}
		
		if(thisServer)
		{
			messageBuffer.add(message);
		}
		
		else
		{
			System.out.println(serverUser.getUser().getUsername() + " > Forwarding message");
			this.sendMessage(message);
		}
		
		System.out.println("------------------------------------------------------------");
		
		//todo handle messages from another server that is destined to another server (NOT this server)
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
	}//END METHOD handleSEND_message(Message)
	
	/**
	 * Handles messages that have GET message type
	 * @param message Message to handle
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
		
		System.out.println("------------------------------------------------------------");
		System.out.println(serverUser.getUser().getUsername() + " > GET from: '" + message.getSource()
			.getUsername() + "'");
		
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
				
				System.out.println("\n" + serverUser.getUser().getUsername() + " > Sending GET to " + GET_MessageBuffer.get(
					i).getDestination().getUsername());
				
				Message getMessage = GET_MessageBuffer.get(i);
				getMessage.setMessageType(MessageType.GET);

				this.sendMessage(getMessage);
				System.out.println(serverUser.getUser().getUsername() + " > GET request sent");
				
			}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
			
		};//END Lambda Runnable FUNCTION
		
		if (messageBuffer.size() > 0)
		{
			new Thread(tempThread).start();
			
		}//END if(messageBuffer.size() > 0)
		
		System.out.println("------------------------------------------------------------");
		
	}//END METHOD handleGET_message(Message)
	
	/**
	 * Handles messages that have ACK message type
	 * @param message Message to handle
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
		System.out.println(serverUser.getUser().getUsername() + " > ACK from: '" + message.getSource().getUsername() + "'");
		System.out.println("\t\tTo: " + message.getDestination().getUsername());
		BigInteger bigInt = (BigInteger) message.getPayload();
		System.out.println("\t\tSequence number: " + (bigInt != null ? bigInt.toString() : null));
		
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
	 * @param message Message to handle
	 */
	//todo implement handleUSERS_message(Message)
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
//		User newUser = (User) message.getPayload();
		UserNetworkInfo newUser = (UserNetworkInfo) message.getPayload();
		System.out.println("\t\t\tUsername: " + newUser.getUser().getUsername());
		System.out.println("\t\t\tserver: " + newUser.getServer().getUsername());
		
		for(UserNetworkInfo curUser : connectedUsers)
		{
//			if(curUser.getUser().getUsername().equals(newUser.getUsername()))
			if(curUser.getUser().getUsername().equals(newUser.getUser().getUsername()))
			{
				System.out.println(serverUser.getUser().getUsername() + " > User already exists");
				userExists = true;
				break;
			}
			
		}
		
		//if it's a new user.
		if(!userExists)
		{
			connectedUsers.add(newUser);
			
			//tell all clients connected to the server
			if (connectedUsers.size() >= 2)
			{
				for(UserNetworkInfo curClient : connectedUsers)
				{
					if(curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
					{
						Message newClientMessage = new Message(MessageType.USERS,
						                                       serverUser.getUser(),
						                                       curClient.getUser(),
						                                       newUser.getUser());
						
						this.sendMessage(newClientMessage);
//				        this.broadcastUser(newClientMessage);
					}
				}
			}
			
			if(connectedServers.size() > 0)
			{
				
			}
			
		}
		
		System.out.println("------------------------------------------------------------");
		
	}//END METHOD handleUSERS_message(Message)
	
	/**
	 * Handles messages that have CONNECT message type
	 * @param message Message to handle
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
		
		System.out.println("------------------------------------------------------------");
		//get the client and add it to the connectedUser vector
		User            user         = message.getSource();
		UserNetworkInfo clientIPInfo = new UserNetworkInfo(clientIPaddress, clientPort, user);
		
		System.out.println(serverUser.getUser().getUsername() + " > User: '" + user.getUsername() + "' is now CONNECTED");
		System.out.println("\t\tsequence number: " + user.getSequenceNumber());
		
		//todo tell the newly connected client who is currently connected

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
		
		//TODO implement broadcast the newly added user to everyone
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
			for(UserNetworkInfo curClient : connectedUsers)
			{
				if(curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
				{
					Message newClientMessage = new Message(MessageType.USERS,
					                                       serverUser.getUser(),
					                                       curClient.getUser(),
					                                       message.getSource());
					
					this.sendMessage(newClientMessage);
					
				}
//				this.broadcastUser(newClientMessage);
				
			}
			
			//todo do you need broadcastUser(Message) and broadcastUserList(Message)? use one method to send the info to everyone.
			
		}
		
		clientIPInfo.setServer(serverUser.getUser());
		
		for(UserNetworkInfo curServer : connectedServers)
		{
			//tell other severs who just connected
			Message serverMsg = new Message(MessageType.USERS,
			                                serverUser.getUser(),
			                                curServer.getUser(),
			                                clientIPInfo);
			
			//todo test sending client info to another server when a client connects to the server
			this.sendServerMessage(serverMsg);
		}
		connectedUsers.add(clientIPInfo);
		System.out.println("\t\tconnected clients: " + connectedUsers.size());
		
		System.out.println("------------------------------------------------------------");
		
	}//END METHOD handleCONNECT_message(Message)
	
	/**
	 * Handles messages that have DISCONNECT message type
	 * @param message Message to handle
	 */
	private final synchronized void handleDISCONNECT_message(Message message)
	{
		//message format received
		//  Type: DISCONNECT
		//  source: source (the sender)
		//  destination: server
		//  payload: null
		
		//remove the user from connectedUser vector
		//tell everyone connected about who is disconnected:
		//  check if they're 1 or more users connected to the server.
		//      if true:
		//          create a new Message
		//              MessageType: DISCONNECT
		//              source: source
		//              destination: null (will be set later on)
		//              payload: the disconnected user
		//          broadcast the message
		
		System.out.println("------------------------------------------------------------");
		//get the client and remove it from the connectedUser vector
		User user = message.getSource();
		UserNetworkInfo userNetworkInfo = this.getClient(user);
		this.removeClient(user);
		
		System.out.println(serverUser.getUser().getUsername() + " > User: '" + user.getUsername() + "' is now DISCONNECTED");
		
		//broadcast the connectUser vector to all connected users/server
		//TODO implement broadcast the updated userList vector to everyone
		//let everyone know who just disconnected
		if (connectedUsers.size() >= 1)
		{
			//message format sent
			//  Type: DISCONNECT
			//  source: server
			//  destination: all users/servers connected
			//  payload: User object
			for(UserNetworkInfo curClient : connectedUsers)
			{
				if(curClient.getServer().getUsername().equals(serverUser.getUser().getUsername()))
				{
					Message disconnectMessage = new Message(MessageType.DISCONNECT,
					                                        message.getSource(),
					                                        curClient.getUser(),
					                                        message.getSource());
//					this.broadcastUser(disconnectMessage);
					this.sendMessage(disconnectMessage);
					
				}
				
			}
			
			for(UserNetworkInfo curServer : connectedServers)
			{
				if(userNetworkInfo.getServer().getUsername().equals(curServer.getUser().getUsername()))
				{
					continue;
				}
				Message disconnectMessage = new Message(MessageType.DISCONNECT,
				                                        message.getSource(),
				                                        curServer.getUser(),
				                                        message.getSource());
				this.sendServerMessage(disconnectMessage);
				
			}
		}
		System.out.println("------------------------------------------------------------");
		
	}//END METHOD handleDISCONNECT_message(Message)
	
	//todo implement handleSERVER_CONNECT_message(Message)
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
//		UserNetworkInfo newServerInfo = new UserNetworkInfo(clientIPaddress, clientPort, newServer.getUser());
//		connectedServers.add(newServerInfo);
		connectedServers.add(newServer);
		System.out.println(serverUser.getUser().getUsername() + " > SERVER_CONNECT message received");
		System.out.println("\t\tSource: " + message.getSource().getUsername());
		System.out.println("\t\tIP address: " + newServer.getIpAddress().getHostAddress());
		System.out.println("\t\tPort: " + newServer.getPort());
		
		Message ackMessage = new Message(MessageType.ACK_SERVER_CONNECT,
		                                 serverUser.getUser(),
		                                 newServer.getUser(),
		                                 serverUser);
		
//		try
//		{
//			ByteArrayOutputStream byteArra/*yOutputStream = new ByteArrayOutputStream();
//			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
//			oos.writeObject(ackMessage);
//			byte[] sendData = byteArrayOutputStream.toByteArray();
//			DatagramPacket sendPacket = new DatagramPacket(sendData,
//			                                               sendData.length,
//			                                               newServer.getIpAddress(),
//			                                               newServer.getPort());
//
//			udpSocket.send(sendPacket);
//			oos.close();
//			byteArrayOutputStream.close();
		
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
			Runnable userThread  = () ->
			{
				this.broadcastUserList(userMessage);
			};
			
			new Thread(userThread).start();
			
			//todo handleServerConnect_message(Message) tell the new server who is connected.
			
//		}//END TRY BLOCK
		
//		catch(IOException e)
//		{
//			e.printStackTrace();
//			System.out.println(serverUser.getUser().getUsername() +
//				                   " > Unable to write object or send packet: handleSERVER_CONNECT_message(Message)");
//
//		}//END CATCH BLOCK IOException
		
		System.out.println("------------------------------------------------------------");
		
	}//END METHOD handleSERVER_CONNECT_message(Message)
	
	//todo implement handleSERVER_DISCONNECT(Message)
	private final synchronized void handleSERVER_DISCONNECT(Message message)
	{
		//find the server disconnecting
		//  remove it from the connectedServer vector
		
		System.out.println("------------------------------------------------------------");
		System.out.println(serverUser.getUser().getUsername() + " > SERVER_DISCONNECT message received");
		System.out.println("\t\tSource: " + message.getSource().getUsername());
		
		for(int i  = 0; i < connectedServers.size(); i++)
		{
			if(connectedServers.get(i).getUser().getUsername().equals(message.getSource().getUsername()))
			{
				connectedServers.remove(i);
				break;
			}
		}
		
		System.out.println(serverUser.getUser().getUsername() + " > Server: " + message.getSource().getUsername() +
			                   " disconnected");
		System.out.println("------------------------------------------------------------");
		
		
	}//END METHOD handleSERVER_DISCONNECT(Message)
	
	//todo implement handleACK_SERVER_CONNECT_message(Message)
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
		
		for(UserNetworkInfo curServer : connectedServers)
		{
//			if(curServer.getUser().getUsername().equals(message.getSource().getUsername()))
//			if(curServer.getIpAddress().equals(ackServer.getIpAddress()))
			if(curServer.getIpAddress().getHostAddress().equals(ackServer.getIpAddress().getHostAddress()))
			{
//				User newServer = (User) message.getPayload();
				System.out.println(serverUser.getUser().getUsername() + " > User object set");
				curServer.setUser(ackServer.getUser());
			}
			
		}
		
		System.out.println(serverUser.getUser().getUsername() + " > Sending client info. " +
							"connectUsers.size() : " + connectedUsers.size());
		
//		User destination = (User) message.getPayload();
		User destination = ackServer.getUser();
		Message userMessage = new Message(MessageType.USERS,
		                                  serverUser.getUser(),
		                                  destination,
		                                  null);
		Runnable userThread  = () ->
		{
			this.broadcastUserList(userMessage);
		};
		
		new Thread(userThread).start();
		
		System.out.println("------------------------------------------------------------");
		
		
	}//END METHOD handleACK_SERVER_CONNECT_message(Message)
	
	/**
	 * Receives the message and calls the appropiate method to
	 * handle the message
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
			}
			
			case ACK_SERVER_CONNECT:
			{
				this.handleACK_SERVER_CONNECT_message(message);
				break;
			}
			
			case SERVER_DISCONNECT:
			{
				this.handleSERVER_DISCONNECT(message);
				break;
			}
			
			default:
			{
				System.out.println(serverUser.getUser().getUsername() + " > unknown message type from: " + message.getSource()
					.getUsername());
				break;
				
			}//END CASE default
			
		}//END switch(message.getMessageType)
		
	}//END METHOD handleMessage(Message)
	
	public void printServers()
	{
		System.out.println("------------------------------------------------------------");
		if(connectedServers.size() > 0)
		{
			for(UserNetworkInfo curServer : connectedServers)
			{
				System.out.println("Server name: " + curServer.getUser().getUsername());
				System.out.println("\t\tIP address: " + curServer.getIpAddress().getHostAddress());
				System.out.println("\t\tPort: " + curServer.getPort());
			}
		}
		
		else
		{
			System.out.println("No servers connected.");
		}
		
		System.out.println("------------------------------------------------------------");
	}
	
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
		}
	}
	
}//END CLASS Server
