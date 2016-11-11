package client.backend;

import utilities.Message;
import utilities.MessageType;
import utilities.User;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Class used to communicate with the server
 */
public class Client
{
	private static String serverIP;
	private static User   user;
	private int port = 5555;
	private boolean            keepGoing;
	
	//UDP
	private DatagramSocket udpSocket;
	private final int MAX_INCOMING_SIZE = 1024;
	private ListenServer    serverListen;
	private SendGET_request getRequestThread;
	
	
	Client(String serverIP, User user)
	{
		this(serverIP, 5555, user);
		
	}//END DEFAULT CONSTRUCTOR Client(String, User)
	
	Client(String serverIP, int port, User user)
	{
		this.serverIP = serverIP;
		this.port = port;
		this.user = user;
		keepGoing = true;
		
	}//END CONSTRUCTOR Client(String, int, User)
	
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
	
	public int getPort()
	{
		return port;
		
	}//END METHOD getPort()
	
	protected void setPort(int port)
	{
		this.port = port;
		
	}//END METHOD setPort(int)
	
	protected void sendMessage(String textMessage, User destination)
	{
		Message message = new Message(MessageType.SEND, user, destination, textMessage);
		this.sendMessage(message);
		
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
			                                               port);
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
			Message message = new Message(MessageType.CONNECT, user, null, null);
			
			udpSocket = new DatagramSocket(port);
			
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
						//send ACK
						String message = (String) msg.getPayload();
						System.out.println(msg.getSource().getUsername() + " > " + message);
						Message ackMessage = new Message(MessageType.ACK, user, msg.getSource(), null);
						sendMessage(ackMessage);
						
						break;
					}//END CASE GET
					
					case ACK:
					{
						System.out.println(
							user.getUsername() + " > " + "ACK message recieved from " + msg.getSource()
								.getUsername());
						break;
					}//END CASE ACK
					
					case USERS:
					{
						System.out.println(user.getUsername() + " > " + "USERS message recieved.");
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
