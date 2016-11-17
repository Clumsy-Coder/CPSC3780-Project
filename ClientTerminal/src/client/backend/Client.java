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
//		Random rand = new Random();
//		sequenceNumber = new BigInteger(14, rand);
		
	}//END CONSTRUCTOR Client(String, int, User)
	
	Client(String serverIP, int serverPort, int clientPort, User user)
	{
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.clientPort = clientPort;
		this.user = user;
		clientConversations = new Vector<Conversation>();
		
	}//END CONSTRUCTOR Client(String, int, int, User)
	
	public String getServerIP()
	{
		return serverIP;
		
	}//END METHOD getServerIP()
	
	protected void setServerIP(String serverIP)
	{
		this.serverIP = serverIP;
		
	}//END METHOD setServerIP
	
	public User getUser()
	{
		return user;
		
	}//END METHOD getUser()
	
	public int getServerPort()
	{
		return serverPort;
		
	}//END METHOD getServerPort()
	
	protected void setServerPort(int serverPort)
	{
		this.serverPort = serverPort;
		
	}//END METHOD setServerPort(int)
	
	protected void sendMessage(String textMessage, User destination)
	{
		Message message = new Message(MessageType.SEND, user, destination, textMessage);
		//add the message to the conversation
		for (int i = 0; i < clientConversations.size(); i++)
		{
			if (clientConversations.get(i).getRecipient().getUsername().equals(destination.getUsername()))
			{
				clientConversations.get(i).addMesage(message);
				System.out.println("from sendMessage: sequence number: " + clientConversations.get(i).getSequenceNumber());
				message.setSequenceNumber(clientConversations.get(i).getSequenceNumber());
				this.sendMessage(message);
			}
		}
		
		
	}//END METHOD sendMessage(String, User)
	
	private void sendMessage(Message message)
	{
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
			
		}
		catch (IOException e)
		{
//			e.printStackTrace();
			keepGoing = false;
			System.out.println("Unable to write object or send packet : sendMessage(Message)");
		}
		
	}//END METHOD sendMessage(Message)
	
	public boolean connect()
	{
		//connect to the server.
		//send the server your info. The User object
		//  if server responds with the username is already taken,
		//      choose another username
		//not sure if the code is placed here:
		//  update the connectedUser list everytime, the server sends
		//  an update of connected users.
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
			
		}
		catch (SocketException e)
		{
//			e.printStackTrace();
			System.out.println(user.getUsername() + " > unable to create UDP socket : connect()");
			keepGoing = false;
			return keepGoing;
		}
		
		return keepGoing;
		
	}//END METHOD connect()
	
	public void disconnect()
	{
		//send a message to the server that you are disconnecting.
		//disconnect the server.
		//make sure the output and input streams are closed.
		//server will remove current user from the database as connected user
		Message message = new Message(MessageType.DISCONNECT, user, null, null);
		
		this.sendMessage(message);
		keepGoing = false;
		if (udpSocket != null)
		{
			udpSocket.close();
			
		}
		serverListen.stop();
		getRequestThread.stop();
		
	}//END METHOD disconnect()
	
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
			message = (Message) inputStream.readObject();
			
		}
		
		catch (IOException e)
		{
//			e.printStackTrace();
			System.out.println(user.getUsername() + " > Unable to read Message: readMessage()");
			keepGoing = false;
		}
		
		catch (ClassNotFoundException e)
		{
//			e.printStackTrace();
			System.out.println(user.getUsername() + " > Class not found : readMessage()");
			keepGoing = false;
		}
		
		return message;
		
	}//END METHOD readMessage()
	
	public void whosConnected()
	{
		System.out.println(user.getUsername() + " > Who is connected: ");
		
		if(clientConversations.size() == 0)
		{
			System.out.println("\t\tNo clients are connected");
			return;
		}
		
		for (Conversation conv : clientConversations)
		{
			System.out.println("\t\t'" + conv.getRecipient()
				.getUsername() + "' : " + conv.getRecipientSequenceNumber());
		}
		
	}//END METHOD whosConnected()
	
	public boolean isRecipient(String user)
	{
		for(Conversation conv : clientConversations)
		{
			if(conv.getRecipient().getUsername().equals(user))
			{
				return true;
			}
		}
		
		return false;
	}//END METHOD isRecipient(String user)
	
	public Conversation getConversation(String user)
	{
		if(!this.isRecipient(user))
		{
			return null;
		}
		
		for(int i = 0; i < clientConversations.size(); i++)
		{
			if(clientConversations.get(i).getRecipient().getUsername().equals(user))
			{
				return clientConversations.get(i);
			}
		}
		
		return null;
	}
	
	private class ListenServer extends Thread
	{
		public void run()
		{
			while (keepGoing)
			{
				Message msg = readMessage();
				
				switch (msg.getMessageType())
				{
					case GET:
					{
						//get the message
						//print it
						//add the message to the conversation.
						//send ACK
						
						String message = (String) msg.getPayload();
						System.out.println("\n" + msg.getSource().getUsername() + " > " + message);
						System.out.println("\t\tSequence number: " + msg.getSequenceNumber());
						
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
							}
						}
						
						//send ACK back to the sender.
						System.out.println("\t\tSending ACK back : " + msg.getSequenceNumber());
						Message ackMessage = new Message(MessageType.ACK,
						                                 user,
						                                 msg.getSource(),
						                                 msg.getSequenceNumber());
						sendMessage(ackMessage);
						
						break;
					}//END CASE GET
					
					case ACK:
					{
						//increment the sequence number for the conversation with the client.
						System.out.println(
							user.getUsername() + " > " + "ACK message received from " + msg.getSource().getUsername());
						System.out.println("\t\tSequence num : " + msg.getPayload());
						
						//increment sequence number for the conversation with the client
						for (int i = 0; i < clientConversations.size(); i++)
						{
							if (clientConversations.get(i)
								.getRecipient()
								.getUsername()
								.equals(msg.getSource().getUsername()))
							{
								clientConversations.get(i).incrementSequenceNumber();
							}
							
						}
						
						break;
						
					}//END CASE ACK
					
					//when server sends all the connected Clients which client is disconnected. Each message has one user.
					case DISCONNECT:
					{
						//remove them from the conversation vector.
						for (int i = 0; i < clientConversations.size(); i++)
						{
							//removed user:
							User removedUser = (User) msg.getPayload();
							
							//search through each one. find the one that matches the received message
							if (clientConversations.get(i)
								.getRecipient()
								.getUsername()
								.equals(removedUser.getUsername()))
							{
								//remove them from conversation vector
								clientConversations.remove(i);
								System.out.println(user.getUsername() + " > user '" + removedUser.getUsername() + "'");
							}
						}
						
						break;
					}
					
					//when a new user has connected to the server
					case USERS:
					{
						//check if the user already exists
						//if it's a new user
						//  create a new conversation and add it to the conversation vector.
						
						System.out.println(user.getUsername() + " > MessageType: USERS received");
						
						//check if the client is already in conversation vector.
						boolean newUser = true;
						for (int i = 0; i < clientConversations.size(); i++)
						{
							User tempUser = (User) msg.getPayload();
							if (clientConversations.get(i).getRecipient().getUsername().equals(tempUser.getUsername()))
							{
								newUser = false;
								break;
							}
						}
						
						//if new user
						if (newUser)
						{
							//add the new client to the conversation vector.
							User         newClient       = (User) msg.getPayload();
							Conversation newConversation = new Conversation(newClient);
							newConversation.setRecipientSequenceNumber(newClient.getSequenceNumber());
							newConversation.setSequenceNumber(user.getSequenceNumber());
							clientConversations.add(newConversation);
							System.out.println(user.getUsername() + " > " + "client '" + newClient.getUsername() + "' is now CONNECTED");
							System.out.println("\t\tsequence number: " + newClient.getSequenceNumber());
						}
						
						break;
						
					}//END CASE USERS
					
					default:
					{
						System.out.println(
							user.getUsername() + " > " + "unknown MessageType received : " + msg.getMessageType());
						break;
					}
					
				}//END switch(msg.getMessageType)
				
			}//END while(keepGoing)
			
		}//END METHOD run()
		
	}//END INNER CLASS ListenServer
	
	private class SendGET_request extends Thread
	{
		public void run()
		{
			System.out.println("SendGET_request thread started");
			while (keepGoing)
			{
				if (!keepGoing)
				{
					return;
				}
				
				Message message = new Message(MessageType.GET, user, null, null);
				try
				{
					Thread.sleep(2000);
					sendMessage(message);
				}
				
				catch (InterruptedException e)
				{
//					e.printStackTrace();
					keepGoing = false;
					System.out.println("from SendGET_request: Thread.sleep() failed");
					
				}
				
			}//END while(keepGoing)
			
		}//END METHOD run()
		
	}//END INNER CLASS SendGET_request
	
}//END CLASS Client
