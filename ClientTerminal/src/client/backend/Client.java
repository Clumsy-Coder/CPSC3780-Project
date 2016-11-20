package client.backend;

import utilities.Conversation;
import utilities.Message;
import utilities.MessageType;
import utilities.User;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;

/**
 * Class used to communicate with the server
 */
public class Client
{
	private static String serverIP;
	private static User   user;
	private int serverPort = 5555;
	private boolean keepGoing;
	private int clientPort = 8000;
	private Vector<Conversation> clientConversations;
	
	//UDP
	private DatagramSocket udpSocket;
	private final int MAX_INCOMING_SIZE = 1024;
	private ListenServer    serverListen;
	private SendGET_request getRequestThread;
	
	
	Client(String serverIP, User user)
	{
		this(serverIP, 5555, user);
		
	}//END DEFAULT CONSTRUCTOR Client(String, User)
	
	Client(String serverIP, int serverPort, User user)
	{
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.user = user;
		keepGoing = true;
		clientConversations = new Vector<Conversation>();
		
	}//END CONSTRUCTOR Client(String, int, User)
	
	Client(String serverIP, int serverPort, int clientPort, User user)
	{
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.clientPort = clientPort;
		this.user = user;
		clientConversations = new Vector<Conversation>();
		
	}//END CONSTRUCTOR Client(String, int, int, User)
	
	public final String getServerIP()
	{
		return serverIP;
		
	}//END METHOD getServerIP()
	
	protected final void setServerIP(String serverIP)
	{
		this.serverIP = serverIP;
		
	}//END METHOD setServerIP
	
	public final User getUser()
	{
		return user;
		
	}//END METHOD getUser()
	
	public final int getServerPort()
	{
		return serverPort;
		
	}//END METHOD getServerPort()
	
	protected final void setServerPort(int serverPort)
	{
		this.serverPort = serverPort;
		
	}//END METHOD setServerPort(int)
	
	protected final void sendMessage(String textMessage, User destination)
	{
		//Make a Message object
		//      MessageType: SEND
		//      source: user
		//      destination: destination
		//      payload: textMessage
		//      sequenceNumber: conversation sequence number (sender)
		//iterate through the conversation list
		//      find the corresponding conversation (must match the destination username)
		//          set the sequence number for the message
		//          add the message to the conversation
		//          send the message
		
		Message message = new Message(MessageType.SEND, user, destination, textMessage);
		//add the message to the conversation
		for (int i = 0; i < clientConversations.size(); i++)
		{
			if (clientConversations.get(i).getRecipient().getUsername().equals(destination.getUsername()))
			{
				message.setSequenceNumber(clientConversations.get(i).getSequenceNumber());
				clientConversations.get(i).addMesage(message);
				this.sendMessage(message);
			}//END IF for checking the message destination username matches the username in the conversation
			
		}//END for (int i = 0; i < clientConversations.size(); i++)
		
	}//END METHOD sendMessage(String, User)
	
	private final void sendMessage(Message message)
	{
		//setup ByteArrayOutputStream : for converting object output stream into byte array
		//setup ObjectOutputStream : for writing the object (in this case the Message object) into the stream
		//write the object to ObjectOutputStream with the Message object as the parameter
		//convert the object output stream to byte array and store it in a byte array
		//create a UDP packet
		//      byte array
		//      byte array size
		//      IP address destination : the server IP address
		//      port number : the server port number.
		//send the packet
		//close the ObjectOutputStream
		//close the ByteArrayOutputStream
		
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(message);
			byte[] sendData = byteArrayOutputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               InetAddress.getByName(serverIP),
			                                               serverPort);
			udpSocket.send(sendPacket);
			
			oos.close();
			byteArrayOutputStream.close();
			
		}// END TRY BLOCK
		
		catch (IOException e)
		{
//			e.printStackTrace();
			keepGoing = false;
			System.out.println(user.getUsername() + " > Unable to write object or send packet : sendMessage(Message)");
			
		}//END CATCH BLOCK IOException
		
	}//END METHOD sendMessage(Message)
	
	public final boolean connect()
	{
		//Make a message object
		//      MessageType: CONNECT
		//      source: user
		//      destination: server
		//      payload: user
		//          (contains the information about the client connecting.
		//              Username,
		//              firstname,
		//              lastname
		//              initial sequence number)
		//create a UDP socket. set the port number (this is for the client side, so the server can send it properly)
		//start listening the server. In a new thread
		//start sending GET request packets. In a new thread.
		//send the message to the server.
		
		keepGoing = true;
		
		try
		{
			Message message = new Message(MessageType.CONNECT, user, null, user);
			
			udpSocket = new DatagramSocket(clientPort);
			
			serverListen = new ListenServer();
			serverListen.start();
			getRequestThread = new SendGET_request();
			getRequestThread.start();
			
			this.sendMessage(message);
		}//END TRY BLOCK
		
		catch (SocketException e)
		{
//			e.printStackTrace();
			System.out.println(user.getUsername() + " > unable to create UDP socket : connect()");
			keepGoing = false;
			return keepGoing;
			
		}//END CATCH BLOCK SocketException
		
		return keepGoing;
		
	}//END METHOD connect()
	
	public final void disconnect()
	{
		//send a message to the server that you are disconnecting.
		//disconnect the server.
		//make sure the output and input streams are closed.
		//server will remove current user from the database as connected user
		
		//Make a Message object
		//      MessageType: DISCONNECT
		//      source: user
		//      destination: server
		//      payload: null
		//send the message to the server.
		//set the keepGoing flag to false.
		//close the udpSocket if it's not null (just in case).
		//stop listening the server. Stopping the thread.
		//stop sending GET requests. Stopping the thread.
		
		Message message = new Message(MessageType.DISCONNECT, user, null, null);
		
		this.sendMessage(message);
		keepGoing = false;
		if (udpSocket != null)
		{
			udpSocket.close();
			
		}//END if (udpSocket != null)
		
		serverListen.stop();
		getRequestThread.stop();
		
	}//END METHOD disconnect()
	
	private final Message readMessage()
	{
		//NOTE: the procedure for extracting the Message object from UDP packet was obtained online.
		//  I'm just following along.
		
		//create a byte array of size MAX_INCOMING_SIZE for incoming data
		//create UDP packet.
		//      incoming data
		//      incoming data size
		//receive the packet using UDP socket.
		//get the data from UDP packet and store it into a byte array
		//create ByteArrayInputStream, and set the parameter as the data extracted from the packet.
		//create ObjectInputStream, and set the paramater as ByteArrayInputStream
		//extract the Message object
		
		Message message = null;
		try
		{
			byte[]         incomingData   = new byte[MAX_INCOMING_SIZE];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			udpSocket.receive(incomingPacket);  //the method will hold, until a packet is received.
			byte[]               data        = incomingPacket.getData();
			ByteArrayInputStream in          = new ByteArrayInputStream(data);
			ObjectInputStream    inputStream = new ObjectInputStream(in);
			message = (Message) inputStream.readObject();
			
		}//END TRY BLOCK
		
		catch (IOException e)
		{
//			e.printStackTrace();
			System.out.println(user.getUsername() + " > Unable to read Message: readMessage()");
			keepGoing = false;
			
		}//END CATCH BLOCK IOException
		
		catch (ClassNotFoundException e)
		{
//			e.printStackTrace();
			System.out.println(user.getUsername() + " > Class not found : readMessage()");
			keepGoing = false;
			
		}//END CATCH BLOCK ClassNotFoundException
		
		return message;
		
	}//END METHOD readMessage()
	
	public final void whosConnected()
	{
		//check if they're any clients connected.
		//  if clients are connected
		//      display each connect client's username along with the sequence number
		//          NOTE: sequence number increments as a client sends messages and a ACK is sent back.
		//  if no clients are connected
		//      display no clients are connected to the console.
		
		System.out.println(user.getUsername() + " > Who is connected: ");
		
		if(clientConversations.size() == 0)
		{
			System.out.println("\t\tNo users are connected");
			return;
			
		}//END if(clientConversations.size() == 0)
		
		for (Conversation conv : clientConversations)
		{
			System.out.println("\t\t'" + conv.getRecipient()
				.getUsername() + "' : " + conv.getRecipientSequenceNumber());
			
		}//END for (Conversation conv : clientConversations)
		
	}//END METHOD whosConnected()
	
	public final boolean isRecipient(String user)
	{
		//iterate through the conversation list
		//      check if current conversation username matches the parameter
		//          if true: return true;
		//return false because we iterated through the list and none of them matchs the parameter.
		
		for(Conversation conv : clientConversations)
		{
			if(conv.getRecipient().getUsername().equals(user))
			{
				return true;
				
			}//END if(conv.getRecipient().getUsername().equals(user))
			
		}//END for(Conversation conv : clientConversations)
		
		return false;
		
	}//END METHOD isRecipient(String user)
	
	public final Conversation getConversation(String user)
	{
		//check if the parameter given is a client
		//  if false : return null
		//
		//iterate through the conversation list
		//      check if the current conversation username matches the parameter
		//          if true: return the conversation.
		
		if(!this.isRecipient(user))
		{
			return null;
			
		}//END if(!this.isRecipient(user))
		
		for(int i = 0; i < clientConversations.size(); i++)
		{
			if(clientConversations.get(i).getRecipient().getUsername().equals(user))
			{
				return clientConversations.get(i);
				
			}//END if(clientConversations.get(i).getRecipient().getUsername().equals(user))
			
		}//END for(int i = 0; i < clientConversations.size(); i++)
		
		return null;
		
	}//END METHOD getConversation(String)
	
	private final synchronized void handleGET_message(Message msg)
	{
		//print the message
		//iterate through the conversation list
		//      check if the source username matches the conversation
		//          if true:
		//              add the message to the corresponding conversation.
		//              increment recipient sequence number (NOT your own)
		//send ACK back
		//      MessageType: ACK
		//      source: user
		//      destination: destination
		//      payload: message sequence number (the message that was received)
		
		String message = (String) msg.getPayload();
		System.out.println("\n" + msg.getSource().getUsername() + " > " + message);
		
		//add it to the conversation
		for (int i = 0; i < clientConversations.size(); i++)
		{
			if (clientConversations.get(i)
				.getRecipient()
				.getUsername()
				.equals(msg.getSource().getUsername()))
			{
				clientConversations.get(i).addMesage(msg);
				clientConversations.get(i).incrementRecipientSequenceNumber();
			}//END if (clientConversations.get(i)
			 //        .getRecipient()
			 //        .getUsername()
			 //        .equals(msg.getSource().getUsername()))
			
		}//END for (int i = 0; i < clientConversations.size(); i++)
		
		//send ACK back to the sender.
		Message ackMessage = new Message(MessageType.ACK,
		                                 user,
		                                 msg.getSource(),
		                                 msg.getSequenceNumber());
		sendMessage(ackMessage);
		
	}//END METHOD handleGET_message(Message)
	
	private final synchronized void handleACK_message(Message msg)
	{
		//iterate through the conversation list
		//      check if the source of the message matches the conversation
		//          if true: increment the conversation sequence number (your own, NOT the recipient)
		
		for (int i = 0; i < clientConversations.size(); i++)
		{
			if (clientConversations.get(i)
				.getRecipient()
				.getUsername()
				.equals(msg.getSource().getUsername()))
			{
				clientConversations.get(i).incrementSequenceNumber();
			}//END if (clientConversations.get(i)
			 //        .getRecipient()
			 //        .getUsername()
			 //        .equals(msg.getSource().getUsername()))
			
		}//END for (int i = 0; i < clientConversations.size(); i++)
		
	}//END METHOD handleACK_message(Message)
	
	private final synchronized void handleDISCONNECT_message(Message msg)
	{
		//store the payload into a User object
		//iterate through the conversation list
		//      check if the username of the User object matches the conversation
		//          if true:
		//              remove the conversation from the list.
		//              display a message that the client has disconnected from the server.
		
		for (int i = 0; i < clientConversations.size(); i++)
		{
			//removed user:
			User removedUser = (User) msg.getPayload();
			
			if (clientConversations.get(i)
				.getRecipient()
				.getUsername()
				.equals(removedUser.getUsername()))
			{
				clientConversations.remove(i);
				System.out.println("\n" + user.getUsername() + " > user '" + removedUser.getUsername() + "' has disconnect from the server");
				
			}//END if (clientConversations.get(i)
			 //        .getRecipient()
			 //        .getUsername()
			 //        .equals(removedUser.getUsername()))
			
		}//END for (int i = 0; i < clientConversations.size(); i++)
		
	}//END METHOD handleDISCONNECT_message(Message)
	
	private final synchronized void handleUSERS_message(Message msg)
	{
		//create boolean variable. Used as a flag indicating the client already exists. Set it to true
		//iterate through the conversation list
		//      store the payload as a User object
		//      if the username from the User object matches the conversation.
		//          if true:
		//              set the boolean variable to false.
		//              break out of the loop
		//check if the boolean variable is true
		//      if true
		//          store the payload as a User object
		//          create a new Conversation. Providing the User object as parameter
		//          set the recipient sequence number (the number provided)
		//          set the sequence number (for yourself, NOT the recipient)
		//          add the conversation to the conversation list
		//          display a message a new user is now connected to the server
		
		boolean newUser = true;
		for (int i = 0; i < clientConversations.size(); i++)
		{
			User tempUser = (User) msg.getPayload();
			if (clientConversations.get(i).getRecipient().getUsername().equals(tempUser.getUsername()))
			{
				newUser = false;
				break;
				
			}//END if (clientConversations.get(i).getRecipient().getUsername().equals(tempUser.getUsername()))
			
		}//END for (int i = 0; i < clientConversations.size(); i++)
		
		if (newUser)
		{
			User         newClient       = (User) msg.getPayload();
			Conversation newConversation = new Conversation(newClient);
			
			newConversation.setRecipientSequenceNumber(newClient.getSequenceNumber());
			newConversation.setSequenceNumber(user.getSequenceNumber());
			clientConversations.add(newConversation);
			
			System.out.println("\n" + user.getUsername() + " > " + "client '" + newClient.getUsername() + "' is now CONNECTED");
			
		}//END if(newUser)
		
	}//END METHOD handleUSERS_message(Message)
	
	private class ListenServer extends Thread
	{
		//while the flag keepGoing is true
		//      get the message
		//      switch(message type)
		//          GET: handle GET message
		//          ACK: handle ACK message
		//          DISCONNECT: handle DISCONNECT message
		//          USERS: handle USERS message
		//          default: print error message
		
		public void run()
		{
			while (keepGoing)
			{
				Message msg = readMessage();
				
				switch (msg.getMessageType())
				{
					case GET:
					{
						handleGET_message(msg);
						break;
						
					}//END CASE GET
					
					case ACK:
					{
						handleACK_message(msg);
						break;
						
					}//END CASE ACK
					
					case DISCONNECT:
					{
						handleDISCONNECT_message(msg);
						break;
						
					}//END CASE DISCONNECT
					
					//when a new user has connected to the server
					case USERS:
					{
						handleUSERS_message(msg);
						break;
						
					}//END CASE USERS
					
					default:
					{
						System.out.println(
							user.getUsername() + " > " + "unknown MessageType received : " + msg.getMessageType());
						break;
						
					}//END CASE default
					
				}//END switch(msg.getMessageType)
				
			}//END while(keepGoing)
			
		}//END METHOD run()
		
	}//END INNER CLASS ListenServer
	
	private class SendGET_request extends Thread
	{
		//while the flag keepGoing is true
		//      check if flag keepGoing is false
		//          if true: break the loop and exit the method
		//      make a GET message
		//          MessageType: GET
		//          source: user
		//          destination: server
		//          payload: null
		//      pause the thread for GET_REQUEST_INTERVAL milliseconds
		//      send the message
		
		public void run()
		{
			while (keepGoing)
			{
				if (!keepGoing)
				{
					return;
					
				}//END if (!keepGoing)
				
				Message message = new Message(MessageType.GET, user, null, null);
				try
				{
					Thread.sleep(2000);
					sendMessage(message);
					
				}//END TRY BLOCK
				
				catch (InterruptedException e)
				{
//					e.printStackTrace();
					keepGoing = false;
					System.out.println("from SendGET_request: Thread.sleep() failed");
					
				}//END CATCH BLOCK InterruptedException
				
			}//END while(keepGoing)
			
		}//END METHOD run()
		
	}//END INNER CLASS SendGET_request
	
}//END CLASS Client
