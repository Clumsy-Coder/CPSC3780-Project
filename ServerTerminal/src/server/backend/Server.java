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
	private User            serverUser;
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
	private Vector<Message>         GET_MessageBuffer; //for when a client sent a GET request. remove elements once ACK is received for each message
	/**
	 * Contains the IP address for the message received from the client.
	 * Changes everytime a message is received.
	 */
	private InetAddress             clientIPaddress;
	
	/**
	 * Server starts in serverPort 5555
	 */
	Server(String username)
	{
		this(username, 5555);
		
	}//END DEFAULT CONSTRUCTOR Server(String)
	
	/**
	 * Server starts on specified serverPort
	 *
	 * @param serverPort
	 */
	Server(String username, int serverPort)
	{
		serverUser = new User(username);
		this.serverPort = serverPort;
		messageBuffer = new Vector<Message>();
		GET_MessageBuffer = new Vector<Message>();
		connectedUsers = new Vector<UserNetworkInfo>();
		
	}//END CONSTRUCTOR Server(String, serverPort)
	
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
			while (keepGoing)
			{
				if (!keepGoing)
				{
					udpSocket.close();
					System.out.println(serverUser.getUsername() + " > Server stopped.");
					return;
				}//END if(!keepGoing)
				
				Message message = this.readMessage();
				//handle the message if the Message object is not null
				if (message != null)
				{
					this.handleMessage(message);
					
				}//END if(message != null)
				
			}//END while(keepGoing)
			
		}//END TRY BLOCK
		
		catch (SocketException e)
		{
			System.out.println(serverUser.getUsername() + " > Unable to create DatagramSocket");
			
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
		keepGoing = false;
		
	}//END METHOD stopServer()
	
	/**
	 * Used for sending user info to all connected clients.
	 * @param message Message to send.
	 */
	private synchronized void broadcastUserList(@NotNull Message message)
	{
		//iterate through the loop
		//  make a message
		//      MessageType: message type
		//      source: server
		//      destination: user
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
		
	}//END METHOD broadcastUserList(Message)
	
	/**
	 * Returns the user network info of the specified user.
	 * @param user The user being searched for
	 * @return UserNetworkInfo if found. null otherwise.
	 */
	private synchronized UserNetworkInfo getClient(@NotNull User user)
	{
		UserNetworkInfo connectedClient = null;
		
		for (int i = 0; i < connectedUsers.size(); i++)
		{
			if (connectedUsers.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				connectedClient = connectedUsers.get(i);
				return connectedClient;
			}
			
		}
		
		return connectedClient;
	}//END METHOD getClient(User)
	
	/**
	 * Used for removing a client from the list of connected users
	 * @param user Client to remove
	 */
	private synchronized void removeClient(@NotNull User user)
	{
		for (int i = 0; i < connectedUsers.size(); i++)
		{
			if (connectedUsers.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				connectedUsers.remove(i);
				return;
			}
		}
	}//END METHOD removeClient(User)
	
	/**
	 * Used for sending a message to a client.
	 * @param sendMessage The message to be sent.
	 */
	private synchronized void sendMessage(@NotNull Message sendMessage)
	{
//		Message message = new Message(messageType, source, destination, payload);
		
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(sendMessage);
			byte[] sendData = byteArrayOutputStream.toByteArray();
//			InetAddress destinationIP = connectedUsers.get(sendMessage.getDestination()
//				                                                    .getUsername()).getIpAddress();
			InetAddress destinationIP   = this.getClient(sendMessage.getDestination()).getIpAddress();
			int         destinationPort = this.getClient(sendMessage.getDestination()).getPort();
//			int destinationPort = connectedUsers.get(sendMessage.getDestination().getUsername()).getPort();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
//			                                               connectedUsers.get(sendMessage.getDestination()
//				                                                                              .getUsername())
//				                                                                              .getIpAddress(),
                                                           destinationIP,
//			                                               connectedUsers.get(sendMessage.getDestination()
//				                                                                              .getUsername())
//				                                                                              .getPort()
                                                           destinationPort);
			
			udpSocket.send(sendPacket);
			oos.close();
			byteArrayOutputStream.close();
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println(serverUser.getUsername() + " > Unable to write object or send packet");
		}
		
	}//END METHOD sendMessage(MessageType, User, User, Object)
	
	/**
	 * Used for reading incoming messages.
	 * The method will hold until a packet is recieved.
	 * @return The message that was read.
	 */
	private Message readMessage()
	{
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
			System.out.println(serverUser.getUsername() + " > unable to receive packet or read object");
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return message;
		
	}//END METHOD readMessage()
	
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
		//  NOTE: sequence number must be embedded.
		
		System.out.println("------------------------------------------------------------");
		messageBuffer.add(message);
		System.out.println(serverUser.getUsername() + " > Message from: '" + message.getSource()
			.getUsername() + "'");
		System.out.println("\t\tTo: " + message.getDestination().getUsername());
		System.out.println("\t\tContent: " + message.getPayload().toString());
		System.out.println("\t\tSequence number: " + message.getSequenceNumber());
		System.out.println("------------------------------------------------------------");
		
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
		//get the message that is destined to to client B
		//send the message to client B
		System.out.println(serverUser.getUsername() + " > GET from: '" + message.getSource()
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

//						System.out.println(serverUser.getUsername() + " > Sending GET to: " + this.getClient(
//							GET_MessageBuffer.get(i).getDestination()).getIpAddress());
				System.out.println("\n" + serverUser.getUsername() + " > Sending GET to " + GET_MessageBuffer.get(
					i).getDestination().getUsername());
//						Message getMessage = new Message(MessageType.GET,
//						                                 GET_MessageBuffer.get(i).getSource(),
//						                                 GET_MessageBuffer.get(i).getDestination(),
//						                                 GET_MessageBuffer.get(i).getPayload());
				
				Message getMessage = GET_MessageBuffer.get(i);
				getMessage.setMessageType(MessageType.GET);
//						this.sendMessage(MessageType.GET,
//						                 GET_MessageBuffer.get(i).getSource(),
//						                 GET_MessageBuffer.get(i).getDestination(),
//						                 GET_MessageBuffer.get(i).getPayload());
				this.sendMessage(getMessage);
				System.out.println(serverUser.getUsername() + " > GET request sent");
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
		
		System.out.println("------------------------------------------------------------");
		System.out.println(serverUser.getUsername() + " > ACK from: " + message.getSource().getUsername());
		System.out.println("\t\tTo: " + message.getDestination().getUsername());
		BigInteger bigInt = (BigInteger) message.getPayload();
		System.out.println("\t\tSequence number: " + (bigInt != null ?
		                                              bigInt.toString() :
		                                              null));
		//check which message is it being acknowledged for. sequence number, source and destination
		//foreword the ACK message to the original sender
		//remove message from messageBuffer
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

//						this.sendMessage(new Message(MessageType.ACK,
//						                             message.getSource(),
//						                             message.getDestination(),
//						                             ackMessage.getSequenceNumber()));
////                                                     null)); //temporarily don't send  the sequence number.
				System.out.println("\n" + "connectedUserHashT.size() : " + connectedUsers.size());
				this.sendMessage(message);
				System.out.println("\n" + serverUser.getUsername() + " > ACK sent");
				System.out.println("\t\tGET_MessageBuffer.size() : " + GET_MessageBuffer.size());
			}
			
		}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
		
		System.out.println("------------------------------------------------------------");
		
	}//END METHOD handleACK_message(Message)
	
	/**
	 * Handles messages that have USERS message type
	 * @param message Message to handle
	 */
	private final synchronized void handleUSERS_message(@NotNull Message message)
	{
		System.out.println("------------------------------------------------------------");
		//let everyone know who just connected to the server.
		
		//message format received:
		//  Type: USERS
		//  source: server
		//  destination: user (who are connected)
		//  payload: User object (must contain initial sequence num)
		//  NOTE: must contain sequence number.
		
		if (connectedUsers.size() >= 2)
		{
			
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
		
		System.out.println("------------------------------------------------------------");
		//get the client and add it to the connectedUser vector
		User            user         = message.getSource();
		UserNetworkInfo clientIPInfo = new UserNetworkInfo(clientIPaddress, clientPort, user);
//				connectedUsers.put(user.getUsername(), clientIPaddress);
		System.out.println(serverUser.getUsername() + " > User: '" + user.getUsername() + "' is now CONNECTED");
		System.out.println("\t\tsequence number: " + user.getSequenceNumber());
		
		//todo tell the newly connected client who is currently connected
		System.out.println(serverUser.getUsername() + " > connected client size: " + connectedUsers.size());
//				Iterator<UserNetworkInfo> it = connectedUsers.iterator();
		//send the message to all clients connected to the server.
//				while (it.hasNext())
		for (UserNetworkInfo connectedUser : connectedUsers)
		{
//					Map.Entry<String, UserNetworkInfo> pair = (Map.Entry) it.next();
			Message broadcastMessage = new Message(MessageType.USERS,
			                                       serverUser,
			                                       message.getSource(),
//					                                       message.getSource(),
                                                   connectedUser.getUser());
			try
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream    oos  = new ObjectOutputStream(baos);
				oos.writeObject(broadcastMessage);
				byte[] sendData = baos.toByteArray();
//			InetAddress destinationIP = connectedUsers.get(sendMessage.getDestination()
//				                                                    .getUsername()).getIpAddress();
				InetAddress destinationIP   = clientIPInfo.getIpAddress();
				int         destinationPort = clientIPInfo.getPort();
//			int destinationPort = connectedUsers.get(sendMessage.getDestination().getUsername()).getPort();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
				                                               sendData.length,
//			                                               connectedUsers.get(sendMessage.getDestination()
//				                                                                              .getUsername())
//				                                                                              .getIpAddress(),
                                                               destinationIP,
//			                                               connectedUsers.get(sendMessage.getDestination()
//				                                                                              .getUsername())
//				                                                                              .getPort()
                                                               destinationPort);
				
				udpSocket.send(sendPacket);
				oos.close();
			}
			catch (IOException e)
			{
				
			}
//						this.sendMessage(broadcastMessage);
//					it.remove();
			
			
		}
		
		System.out.println(serverUser.getUsername() + " > after iterator: " + connectedUsers.size());
		
		
		//TODO implement broadcast the newly added user to everyone
		//let everyone connected to the server know that a new user has connected to the server.
		//connectedUsersHastT.size() + 1 because you don't want to broadcast to the original sender.
		//  the original sender would think thats a new user.
		int connectUserSize = connectedUsers.size();
		int connectNewUser  = connectUserSize + 1;
		System.out.println(serverUser.getUsername() + " > connectedUserSize : " + connectUserSize);
		System.out.println(serverUser.getUsername() + " > connectedUserHashT.size() + 1 : " + connectNewUser);
		if (connectedUsers.size() + 1 >= 2)
		{
			System.out.println(serverUser.getUsername() + " > sending info to all connected users about new user. ");
			//message format sent
			//  Type: USERS
			//  source: server
			//  destination: all users/servers connected
			//  payload: User object
			//  NOTE: must contain sequence number
			
			Message newClientMessage = new Message(MessageType.USERS,
			                                       serverUser,
			                                       null,
			                                       message.getSource());
			this.broadcastUserList(newClientMessage);
			
		}
//				connectedUsers.put(user, clientIPInfo);
		connectedUsers.add(clientIPInfo);
		System.out.println(serverUser.getUsername() + " > new user added");
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
		
		System.out.println("------------------------------------------------------------");
		//get the client and remove it from the connectedUser vector
		User user = message.getSource();
//				connectedUsers.remove(user.getUsername());
		this.removeClient(user);
		System.out.println(serverUser.getUsername() + " > User: '" + user.getUsername() + "' is now DISCONNECTED");
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
			
			Message disconnectMessage = new Message(MessageType.DISCONNECT,
			                                        message.getSource(),
			                                        null,
			                                        message.getSource());
			this.broadcastUserList(disconnectMessage);
		}
		System.out.println("------------------------------------------------------------");
		
	}//END METHOD handleDISCONNECT_message(Message)
	
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
		//          send it to the client.
		//  ACK
		//      find the message being acknowledged, remove it from the messageBuffer
		//          and GET_messageBuffer and forward ACK to the original sender of the message.
		//  USERS
		//  CONNECT
		//      get the username and ipaddress and add it to the hashtable.
		//      send the updated user list to everyone connected.
		//  DISCONNECT
		//      get the username and remove it from hashtable.
		//      send the updated user list to everyone connected.
		switch (message.getMessageType())
		{
			case SEND:
			{
//				//get the message and store it in messageBuffer
//				//message format received:
//				//  Type: SEND
//				//  source: source (the sender)
//				//  destination: destination (the receiver)
//				//  payload: text message
//				//  NOTE: sequence number must be embedded.
//				System.out.println("------------------------------------------------------------");
//				messageBuffer.add(message);
//				System.out.println(serverUser.getUsername() + " > Message from: '" + message.getSource()
//					.getUsername() + "'");
//				System.out.println("\t\tTo: " + message.getDestination().getUsername());
//				System.out.println("\t\tContent: " + message.getPayload().toString());
//				System.out.println("\t\tSequence number: " + message.getSequenceNumber());
//				System.out.println("------------------------------------------------------------");
				
				this.handleSEND_message(message);
				
				break;
				
			}//END CASE SEND
			
			case GET:
			{
//				//message format received:
//				//  Type: GET
//				//  source: source (the sender)
//				//  destination: server
//				//  payload: null
//
//				//get the message that is destined to client B
//				//remove it from messageBuffer and place it in GET_MessageBuffer
//				//send the messages destined to client B from GET_MessageBuffer
//
//				System.out.println("------------------------------------------------------------");
//				//get the message that is destined to to client B
//				//send the message to client B
//				System.out.println(serverUser.getUsername() + " > GET from: '" + message.getSource()
//					.getUsername() + "'");
//				//using a new thread in case there's multiple messages that needs to be sent
//				Runnable tempThread = () ->
//				{
//					for (int i = 0; i < messageBuffer.size(); i++)
//					{
//						//get the message that is destined to client B
//						if (messageBuffer.get(i)
//							.getDestination()
//							.getUsername()
//							.equals(message.getSource().getUsername()))
//						{
//							GET_MessageBuffer.add(messageBuffer.remove(i));
//						}//END IF messageBuffer username at i is equal to message
//
//					}//END for (int i = 0; i < messageBuffer.size(); i++)
//
//					//send each message to Client B
//					for (int i = 0; i < GET_MessageBuffer.size(); i++)
//					{
//						//message format sent
//						//  Type: GET
//						//  source: source (the one who the sent the SEND message)
//						//  destination: destination (the one who sent the GET message)
//						//  payload: text message
//						//  NOTE: sequence number is embedded.
//
////						System.out.println(serverUser.getUsername() + " > Sending GET to: " + this.getClient(
////							GET_MessageBuffer.get(i).getDestination()).getIpAddress());
//						System.out.println("\n" + serverUser.getUsername() + " > Sending GET to " + GET_MessageBuffer.get(
//							i).getDestination().getUsername());
////						Message getMessage = new Message(MessageType.GET,
////						                                 GET_MessageBuffer.get(i).getSource(),
////						                                 GET_MessageBuffer.get(i).getDestination(),
////						                                 GET_MessageBuffer.get(i).getPayload());
//
//						Message getMessage = GET_MessageBuffer.get(i);
//						getMessage.setMessageType(MessageType.GET);
////						this.sendMessage(MessageType.GET,
////						                 GET_MessageBuffer.get(i).getSource(),
////						                 GET_MessageBuffer.get(i).getDestination(),
////						                 GET_MessageBuffer.get(i).getPayload());
//						this.sendMessage(getMessage);
//						System.out.println(serverUser.getUsername() + " > GET request sent");
//					}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
//
//				};//END Lambda Runnable FUNCTION
//
//				if (messageBuffer.size() > 0)
//				{
//					new Thread(tempThread).start();
//
//				}//END if(messageBuffer.size() > 0)
//
//				System.out.println("------------------------------------------------------------");
				
				this.handleGET_message(message);
				break;
				
			}//END CASE GET
			
			case ACK:
			{
//				//message format received
//				//  Type: ACK
//				//  source: source (the sender)
//				//  destination: destination (who is the sender sending to)
//				//  payload: sequence number
//
//				System.out.println("------------------------------------------------------------");
//				System.out.println(serverUser.getUsername() + " > ACK from: " + message.getSource().getUsername());
//				System.out.println("\t\tTo: " + message.getDestination().getUsername());
//				BigInteger bigInt = (BigInteger) message.getPayload();
//				System.out.println("\t\tSequence number: " + (bigInt != null ?
//				                                              bigInt.toString() :
//				                                              null));
//				//check which message is it being acknowledged for. sequence number, source and destination
//				//foreword the ACK message to the original sender
//				//remove message from messageBuffer
//				BigInteger sequenceNum = (BigInteger) message.getPayload();
//
//				for (int i = 0; i < GET_MessageBuffer.size(); i++)
//				{
//					//message format sent
//					//  Type: ACK
//					//  source: source (the one who sent ACK message)
//					//  destination: destination (the one who sent the SEND message)
//					//  payload: sequence number
//
//					//check for the sequence number, source and destination
//					if (GET_MessageBuffer.get(i).getSequenceNumber().compareTo(sequenceNum) == 0 &&
//						GET_MessageBuffer.get(i).getSource().getUsername().equals(message.getDestination().getUsername()) &&
//						GET_MessageBuffer.get(i).getDestination().getUsername().equals(message.getSource().getUsername()))
//					{
//						Message ackMessage = GET_MessageBuffer.remove(i);
//
////						this.sendMessage(new Message(MessageType.ACK,
////						                             message.getSource(),
////						                             message.getDestination(),
////						                             ackMessage.getSequenceNumber()));
//////                                                     null)); //temporarily don't send  the sequence number.
//						System.out.println("\n" + "connectedUserHashT.size() : " + connectedUsers.size());
//						this.sendMessage(message);
//						System.out.println("\n" + serverUser.getUsername() + " > ACK sent");
//						System.out.println("\t\tGET_MessageBuffer.size() : " + GET_MessageBuffer.size());
//					}
//
//				}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
//
//				System.out.println("------------------------------------------------------------");
				
				this.handleACK_message(message);
				
				break;
				
			}//END CASE ACK
			
			case USERS:
			{
//				System.out.println("------------------------------------------------------------");
//				//let everyone know who just connected to the server.
//
//				//message format received:
//				//  Type: USERS
//				//  source: server
//				//  destination: user (who are connected)
//				//  payload: User object (must contain initial sequence num)
//				//  NOTE: must contain sequence number.
//
//				if (connectedUsers.size() >= 2)
//				{
//
//				}
//
//				System.out.println("------------------------------------------------------------");
				
				this.handleUSERS_message(message);
				break;
				
			}//END CASE USERS
			
			case CONNECT:
			{
//				//message format recieved
//				//  Type: CONNECT
//				//  source: source (the one who is connecting)
//				//  destination: server
//				//  payload: User object
//				//  NOTE: must contain sequence number
//
//				System.out.println("------------------------------------------------------------");
//				//get the client and add it to the connectedUser vector
//				User            user         = message.getSource();
//				UserNetworkInfo clientIPInfo = new UserNetworkInfo(clientIPaddress, clientPort, user);
////				connectedUsers.put(user.getUsername(), clientIPaddress);
//				System.out.println(serverUser.getUsername() + " > User: '" + user.getUsername() + "' is now CONNECTED");
//				System.out.println("\t\tsequence number: " + user.getSequenceNumber());
//
//				//todo tell the newly connected client who is currently connected
//				System.out.println(serverUser.getUsername() + " > connected client size: " + connectedUsers.size());
////				Iterator<UserNetworkInfo> it = connectedUsers.iterator();
//				//send the message to all clients connected to the server.
////				while (it.hasNext())
//				for (UserNetworkInfo connectedUser : connectedUsers)
//				{
////					Map.Entry<String, UserNetworkInfo> pair = (Map.Entry) it.next();
//					Message broadcastMessage = new Message(MessageType.USERS,
//					                                       serverUser,
//					                                       message.getSource(),
////					                                       message.getSource(),
//                                                           connectedUser.getUser());
//					try
//					{
//						ByteArrayOutputStream baos = new ByteArrayOutputStream();
//						ObjectOutputStream    oos  = new ObjectOutputStream(baos);
//						oos.writeObject(broadcastMessage);
//						byte[] sendData = baos.toByteArray();
////			InetAddress destinationIP = connectedUsers.get(sendMessage.getDestination()
////				                                                    .getUsername()).getIpAddress();
//						InetAddress destinationIP   = clientIPInfo.getIpAddress();
//						int         destinationPort = clientIPInfo.getPort();
////			int destinationPort = connectedUsers.get(sendMessage.getDestination().getUsername()).getPort();
//						DatagramPacket sendPacket = new DatagramPacket(sendData,
//						                                               sendData.length,
////			                                               connectedUsers.get(sendMessage.getDestination()
////				                                                                              .getUsername())
////				                                                                              .getIpAddress(),
//                                                                       destinationIP,
////			                                               connectedUsers.get(sendMessage.getDestination()
////				                                                                              .getUsername())
////				                                                                              .getPort()
//                                                                       destinationPort);
//
//						udpSocket.send(sendPacket);
//						oos.close();
//					}
//					catch (IOException e)
//					{
//
//					}
////						this.sendMessage(broadcastMessage);
////					it.remove();
//
//
//				}
//
//				System.out.println(serverUser.getUsername() + " > after iterator: " + connectedUsers.size());
//
//
//				//TODO implement broadcast the newly added user to everyone
//				//let everyone connected to the server know that a new user has connected to the server.
//				//connectedUsersHastT.size() + 1 because you don't want to broadcast to the original sender.
//				//  the original sender would think thats a new user.
//				int connectUserSize = connectedUsers.size();
//				int connectNewUser  = connectUserSize + 1;
//				System.out.println(serverUser.getUsername() + " > connectedUserSize : " + connectUserSize);
//				System.out.println(serverUser.getUsername() + " > connectedUserHashT.size() + 1 : " + connectNewUser);
//				if (connectedUsers.size() + 1 >= 2)
//				{
//					System.out.println(serverUser.getUsername() + " > sending info to all connected users about new user. ");
//					//message format sent
//					//  Type: USERS
//					//  source: server
//					//  destination: all users/servers connected
//					//  payload: User object
//					//  NOTE: must contain sequence number
//
//					Message newClientMessage = new Message(MessageType.USERS,
//					                                       serverUser,
//					                                       null,
//					                                       message.getSource());
//					this.broadcastUserList(newClientMessage);
//
//				}
////				connectedUsers.put(user, clientIPInfo);
//				connectedUsers.add(clientIPInfo);
//				System.out.println(serverUser.getUsername() + " > new user added");
//				System.out.println("\t\tconnected clients: " + connectedUsers.size());
//
//				System.out.println("------------------------------------------------------------");
				
				this.handleCONNECT_message(message);
				break;
				
			}//END CASE CONNECT
			
			case DISCONNECT:
			{
//				//message format received
//				//  Type: DISCONNECT
//				//  source: source (the sender)
//				//  destination: server
//				//  payload: null
//
//				System.out.println("------------------------------------------------------------");
//				//get the client and remove it from the connectedUser vector
//				User user = message.getSource();
////				connectedUsers.remove(user.getUsername());
//				this.removeClient(user);
//				System.out.println(serverUser.getUsername() + " > User: '" + user.getUsername() + "' is now DISCONNECTED");
//				//broadcast the connectUser vector to all connected users/server
//				//TODO implement broadcast the updated userList vector to everyone
//				//let everyone know who just disconnected
//				if (connectedUsers.size() >= 1)
//				{
//					//message format sent
//					//  Type: DISCONNECT
//					//  source: server
//					//  destination: all users/servers connected
//					//  payload: User object
//
//					Message disconnectMessage = new Message(MessageType.DISCONNECT,
//					                                        message.getSource(),
//					                                        null,
//					                                        message.getSource());
//					this.broadcastUserList(disconnectMessage);
//				}
//				System.out.println("------------------------------------------------------------");
				
				this.handleDISCONNECT_message(message);
				
				break;
				
			}//END CASE DISCONNECT
			
			default:
			{
				System.out.println(serverUser.getUsername() + " > unknown message type from: " + message.getSource()
					.getUsername());
				break;
				
			}//END CASE default
			
		}//END switch(message.getMessageType)
		
	}//END METHOD handleMessage(Message)
	
}//END CLASS Server
