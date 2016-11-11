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
	private static String             serverIP;
	private static User               user;
	private        int                port = 5555;
	private        ArrayList<User>    connectedUsers;
	private        ObjectOutputStream sOutput;  // I/O
	private        ObjectInputStream  sInput;   // I/O
	private        Socket             socket;   // I/O
	private        boolean            keepGoing;
	
	//UDP
	private DatagramSocket udpSocket;
	private final int MAX_INCOMING_SIZE = 1024;
	private final int MAX_OUTGOING_SIZE = 1024;
	private ListenFromServer serverListen;
	private SendGET_request  getRequestThread;
	
	
	Client(String serverIP, User user)
	{
		this(serverIP, 5555, user);
	}
	
	Client(String serverIP, int port, User user)
	{
		this.serverIP = serverIP;
		this.port = port;
		this.user = user;
		keepGoing = true;
	}
	
	public String getServerIP()
	{
		return serverIP;
	}
	
	protected void setServerIP(String serverIP)
	{
		this.serverIP = serverIP;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public int getPort()
	{
		return port;
	}
	
	protected void setPort(int port)
	{
		this.port = port;
	}
	
	protected void sendMessage(String textMessage, User destination)
	{
		Message message = new Message(MessageType.SEND, user, destination, textMessage);
//		try
//		{
//			sOutput.writeObject(message);
		this.sendMessage(message);
//		}
//		catch (IOException e)
//		{
////			e.printStackTrace();
//			System.out.println("Unable to send message from : " + message.getSource().getUsername().toString());
//			System.out.println("Message content: " + message.getPayload().toString());
//		}
	}
	
	private void sendMessage(Message message)
	{
		
		try
		{
//			System.out.println(user.getUsername() + " > to: " + ((message.getDestination() != null) ? message.getDestination().getUsername() : serverIP) );
//			System.out.println("\t\tDestination IP address: " + InetAddress.getByName(serverIP));
//			System.out.println("\t\tPort: " + port);
			
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(message);
			byte[]         sendData   = byteArrayOutputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               InetAddress.getByName(serverIP),
			                                               port);
			udpSocket.send(sendPacket);

//			udpSocket.close();
			oos.close();
			byteArrayOutputStream.close();
			
		}
		catch (IOException e)
		{
//			e.printStackTrace();
			keepGoing = false;
			System.out.println("Unable to write object or send packet : sendMessage(Message)");
		}
		
	}
	
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
//		try
//		{
//			socket = new Socket(serverIP, port);
//			System.out.println("Connection accepted " + socket.getInetAddress() + ":" + this.getPort());
//
//			sInput = new ObjectInputStream(socket.getInputStream());
//			sOutput = new ObjectOutputStream(socket.getOutputStream());
//
//		}
//		catch (IOException e)
//		{
////			e.printStackTrace();
//			System.out.println("Could not setup socket, input stream or output stream");
//			keepGoing = false;
//			return keepGoing;
//		}
//
//		new ListenFromServer().start();
//		System.out.println("Listening to the server");
//		new SendGET_request().start();
//		System.out.println("SendGET_request thread started");
//
//		try
//		{
//			Message message = new Message(MessageType.CONNECT, user, null, null);
//			sOutput.writeObject(message);
//		}
//		catch (IOException e)
//		{
////			e.printStackTrace();
//			System.out.println("Unable to send initial message. Disconnecting");
//			disconnect();
//			return keepGoing;
//		}
		
		try
		{
			udpSocket = new DatagramSocket(port);
//			InetAddress           ipaddress    = InetAddress.getLocalHost();
			Message message = new Message(MessageType.CONNECT, user, null, null);
//			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//			ObjectOutputStream    oos          = new ObjectOutputStream(outputStream);
//			oos.writeObject(message);
//			byte[]         data   = outputStream.toByteArray();
//			DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(serverIP), 5555);
//			udpSocket.send(packet);
			
			serverListen = new ListenFromServer();
			serverListen.start();
			System.out.println(user.getUsername() + " > listening to server");
			getRequestThread = new SendGET_request();
			getRequestThread.start();
			this.sendMessage(message);
//			System.out.println(user.getUsername() + " > SendGET_request thread started");
			
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			System.out.println(user.getUsername() + " > unable to create UDP socket : connect()");
			keepGoing = false;
			return keepGoing;
		}
//		catch (UnknownHostException e)
//		{
////			e.printStackTrace();
//			System.out.println(user.getUsername() + " > unable to get ipaddress");
//			keepGoing = false;
//		}
//		catch (IOException e)
//		{
////			e.printStackTrace();
//			System.out.println(user.getUsername() + " > unable to create ObjectOutputStream or unable to write object");
//			keepGoing = false;
//		}
		
		return keepGoing;
		
	}//END METHOD connect()
	
	public void disconnect()
	{
		//send a message to the server that you are disconnecting.
		//disconnect the server.
		//make sure the output and input streams are closed.
		//server will remove current user from the database as connected user
		Message message = new Message(MessageType.DISCONNECT, user, null, null);
//		try
//		{
//			//send a message to the server that you are disconnecting.
//			sOutput.writeObject(message);
//			//close the I/O streams
//			if (sInput != null)
//			{
//				sInput.close();
//			}
//			if (sOutput != null)
//			{
//				sOutput.close();
//			}
//			if (socket != null)
//			{
//				socket.close();
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
		this.sendMessage(message);
		keepGoing = false;
		if (udpSocket != null)
		{
			udpSocket.close();
			
		}
		serverListen.stop();
		getRequestThread.stop();
		System.out.println(user.getUsername() + " > Connection closed");
		
	}
	
	private void sendGetRequest()
	{
		//send a get request every n milliseconds.
		//if the server replies, update the message list.
	}
	
	private Message readMessage()
	{
		Message message = null;
		try
		{
			System.out.println("Message read 1 ");
			byte[] incomingData = new byte[MAX_INCOMING_SIZE];
			
			System.out.println("Message read 2 ");
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			
			System.out.println("Message read 3 ");
			udpSocket.receive(incomingPacket);
			
			System.out.println("Message read 4 ");
			byte[] data = incomingPacket.getData();
			
			System.out.println("Message read 5 ");
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			
			System.out.println("Message read 6 ");
			ObjectInputStream inputStream = new ObjectInputStream(in);
			
			//read the object
			System.out.println("Message read 7 ");
			message = (Message) inputStream.readObject();
			
			System.out.println("Message read 8");
//				inputStream.close();
//				in.close();
//			udpSocket.close();
			
			
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
	}
	
	private class ListenFromServer extends Thread
	{
		public void run()
		{
			System.out.println("ListenFromServer thread started");
			while (keepGoing)
			{
				System.out.println(keepGoing);
//				try
//				{
//					udpSocket = new DatagramSocket();
//				}
//				catch (SocketException e)
//				{
//					e.printStackTrace();
//				}
//				if (!keepGoing)
//				{
//					return;
//				}
//				try
//				{
//					Message msg = (Message) sInput.readObject();
				System.out.println("Calling readMessage");
				Message msg = readMessage();
				System.out.println("after readMessage");
				//check what type of message it is.
				//possible types received:
				//  GET
				//  ACK
				//  USERS
				System.out.println(user.getUsername() + " > MessageType: " + msg.getMessageType());
//				if (msg == null)
//				{
//					continue;
//				}
				
				
				switch (msg.getMessageType())
				{
					
					case GET:
					{
						//System.out.println("\n" + user.getUsername() + " > " + "GET request replied");
//							Vector<Message> msgList = (Vector<Message>) msg.getPayload();
//							for (int i = 0; i < msgList.size(); i++)
//							{
//								System.out.println(
//									msg.getSource().getUsername() + " > " + msgList.get(i).getPayload() + "\n");
//							}
//
//							if (msgList.size() > 0)
//							{
//								//send a ACK
//								Message message = new Message(MessageType.ACK, user, msg.getSource(), null);
//								sOutput.writeObject(message);
//
//							}
						
						//get the message
						//print it
						//send ACK
						System.out.println(user.getUsername() + " > inside GET switch");
						String message = (String) msg.getPayload();
						System.out.println(msg.getSource().getUsername() + " > " + message);
						Message ackMessage = new Message(MessageType.ACK, user, msg.getSource(), null);
						sendMessage(ackMessage);
						
						break;
					}
					case ACK:
					{
						System.out.println(
							user.getUsername() + " > " + "ACK message recieved from " + msg.getSource()
								.getUsername());
						break;
					}
					case USERS:
					{
						System.out.println(user.getUsername() + " > " + "USERS message recieved.");
						break;
					}
					default:
					{
						System.out.println(
							user.getUsername() + " > " + "unknown MessageType received : " + msg.getMessageType());
						break;
					}
				}

//				}
//				catch (IOException e)
//				{
////					e.printStackTrace();
//					System.out.println(user.getUsername() + " > Connection closed. Exiting");
//					keepGoing = false;
//				}
//				catch (ClassNotFoundException e)
//				{
//					e.printStackTrace();
//				}
			}
		}
	}
	
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
//					sOutput.writeObject(message);
					sendMessage(message);
				}
//				catch (IOException e)
//				{
////					e.printStackTrace();
//					System.out.println("from SendGET_request: closing thread.");
//				}
				catch (InterruptedException e)
				{
//					e.printStackTrace();
					keepGoing = false;
					System.out.println("from SendGET_request: Thread.sleep() failed");
					
				}
			}
		}
	}
	
}//END CLASS Client
