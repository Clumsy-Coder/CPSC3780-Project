package server.backend;

import utilities.Message;
import utilities.MessageType;
import utilities.User;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Vector;


public class Server
{
	private int port = 5555;
	private SimpleDateFormat     sdf;
	private ServerSocket         serverSocket;
	private boolean              keepGoing;
	private Vector<ClientThread> clientThreadList;
	private Vector<Message>      messageBuffer;
	private Message              msg;
	private User                 serverUser;
	
	//UDP
	private DatagramSocket socket;
	private final int MAX_INCOMING_SIZE = 1024;
	private final int MAX_OUTGOING_SIZE = 1024;
	private Vector<User>                   connectedUser;
	private Hashtable<String, InetAddress> connectedUsersHashT;
	private Vector<Message>                GET_MessageBuffer; //for when a client sent a GET request. remove elements once ACK is recieved for each message
	private InetAddress                    clientIPaddress;
	
	/**
	 * Server starts in port 5555
	 */
	Server(String username)
	{
		this(username, 5555);
	}
	
	/**
	 * Server starts on specified port
	 *
	 * @param port
	 */
	Server(String username, int port)
	{
		serverUser = new User(username);
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		clientThreadList = new Vector<ClientThread>();
		messageBuffer = new Vector<Message>();
		connectedUser = new Vector<User>();
		GET_MessageBuffer = new Vector<Message>();
		connectedUsersHashT = new Hashtable<String, InetAddress>();
	}
	
	private ClientThread findUser(User user)
	{
		for (int i = 0; i < clientThreadList.size(); i++)
		{
			if (clientThreadList.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				return clientThreadList.get(i);
			}
		}
		
		return null;
	}
	
	private int removeMessage(Message message)
	{
		//find the message based on the sequence number AND the sender
		for (int i = 0; i < messageBuffer.size(); i++)
		{
			//if current message has the matching sequence number and sender
			if (messageBuffer.get(i).getSequenceNumber() == message.getSequenceNumber() &&
				messageBuffer.get(i).getSource().getUsername().equals(message.getSource().getUsername()))
			{
				messageBuffer.remove(i);
				return i;
			}
		}
		//if message cannot be found
		return -1;
	}
	
	private Vector<Message> findMessages(User user)
	{
		Vector<Message> userMessages = new Vector<Message>();
		for (Message curMessage : messageBuffer)
		{
			if (curMessage.getDestination().getUsername().equals(user.getUsername()))
			{
				userMessages.add(curMessage);
			}
		}
		
		return userMessages;
	}
	
	public void startServer()
	{
//		serverSocket = new ServerSocket(port);
//		System.out.println("Server up and running on port: " + port);
//		keepGoing = true;
//
//		while (keepGoing)
//		{
//			//accept the socket
//			System.out.println("Server waiting for Clients on port: " + port);
//			Socket socket = serverSocket.accept();
//
//			//for stopping the server
//			if (!keepGoing)
//			{
//				break;
//			}
//
//			ClientThread t = new ClientThread(socket);
//			clientThreadList.add(t);
//			t.start();
//
//		}
//
//		//close the server socket
//		serverSocket.close();
//		for (int i = 0; i < clientThreadList.size(); i++)
//		{
//			clientThreadList.get(i).sInput.close();
//			clientThreadList.get(i).sOutput.close();
//			clientThreadList.get(i).socket.close();
//		}
//
//		System.out.println("Server stopped.");
		
		try
		{
			socket = new DatagramSocket(port);
			System.out.println("Server up and running on port: " + Inet4Address.getLocalHost()
				.getHostAddress() + ":" + port);
//			byte[] incomingData = new byte[MAX_INCOMING_SIZE];
			keepGoing = true;
			while (keepGoing)
			{
				if (!keepGoing)
				{
					socket.close();
					System.out.println(serverUser.getUsername() + " > Server stopped.");
					return;
				}
//				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
//				socket.receive(incomingPacket);
//				byte[]               data        = incomingPacket.getData();
//				ByteArrayInputStream in          = new ByteArrayInputStream(data);
//				ObjectInputStream    inputStream = new ObjectInputStream(in);
//				//read the object
//				Message message = (Message) inputStream.readObject();
				Message message = this.readMessage();
				//handle the message if the Message object is not null
				if (message != null)
				{
//					this.handleMessage(message, incomingPacket.getAddress());
					this.handleMessage(message);
				}
			}
			
		}
		catch (SocketException e)
		{
			System.out.println(serverUser.getUsername() + " > Unable to create DatagramSocket");
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
//		catch (IOException e)
//		{
//			System.out.println(serverUser.getUsername() + " > Unable to receive packet.");
//		}
//		catch (ClassNotFoundException e)
//		{
//			System.out.println(serverUser.getUsername() + " > Unable to read Message object");
//		}
		
	}
	
	public void stopServer() throws
	                         IOException
	{
		keepGoing = false;
//		new Socket("localhost", port);
	}
	
	//TODO for the client who has logged off
	private synchronized void logoffUser(User user)
	{
		//update the client thread
		//broadcast the update userlist to all connected users
		for (int i = 0; i < clientThreadList.size(); i++)
		{
			if (clientThreadList.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				clientThreadList.remove(i);
				return;
			}
		}
		
	}
	
	//TODO implement the broadcastUserList method to send every user their updated userList
	private synchronized void broadcastUserList()
	{
		
	}
	
	private synchronized void sendMessage(MessageType messageType, User source, User destination, Object payload)
	{
		Message message = new Message(messageType, source, destination, payload);
		
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(message);
			byte[] sendData  = byteArrayOutputStream.toByteArray();
			String ipaddress = connectedUsersHashT.get(destination.getUsername()).toString();
			System.out.println("IPaddress: " + ipaddress);
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               connectedUsersHashT.get(destination.getUsername()),
			                                               port);
			System.out.println("Sending to: " + destination.getUsername() + " : " + connectedUsersHashT.get(destination.getUsername()));
			System.out.println("\t\tsendData.size() : " + sendData.length);
			System.out.println("\t\tPacket size: " + sendPacket.getLength());
			
			socket.send(sendPacket);
			oos.close();
			byteArrayOutputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private Message readMessage()
	{
		Message message = null;
		try
		{
			byte[]         incomingData   = new byte[MAX_INCOMING_SIZE];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			socket.receive(incomingPacket);
			byte[]               data        = incomingPacket.getData();
			ByteArrayInputStream in          = new ByteArrayInputStream(data);
			ObjectInputStream    inputStream = new ObjectInputStream(in);
			//read the object
			message = (Message) inputStream.readObject();
			clientIPaddress = incomingPacket.getAddress();
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return message;
	}
	
	private synchronized void handleMessage(Message message)
	{
		//handle the message based on the type.
		//possible type:
		//  SEND
		//  GET
		//  ACK
		//  USERS
		//  CONNECT
		//  DISCONNECT
		switch (message.getMessageType())
		{
			case SEND:
			{
				System.out.println("------------------------------------------------------------");
				//get the message and store it in messageBuffer
				System.out.println(serverUser.getUsername() + " > message from: " + message.getSource().getUsername());
				System.out.println("\t\tTo: " + message.getDestination().getUsername());
				System.out.println("\t\tContent: " + message.getPayload().toString());
				messageBuffer.add(message);
				System.out.println("------------------------------------------------------------");
				break;
			}
			case GET:
			{
//				System.out.println("------------------------------------------------------------");
				//get the message that is destined to to client B
				//send the message to client B
				
				//using a new thread in case there's multiple messages that needs to be sent
//				System.out.println(serverUser.getUsername() + " > GET request from " + message.getSource().getUsername());
				Runnable tempThread = () ->
				{
					System.out.println("running tempThread");
					for (int i = 0; i < messageBuffer.size(); i++)
					{
						//get the message that is destined to client B
						if (messageBuffer.get(i)
							.getDestination()
							.getUsername()
							.equals(message.getSource().getUsername()))
						{
							GET_MessageBuffer.add(messageBuffer.remove(i));
						}
					}
					
					//send each message to Client B
					for (int i = 0; i < GET_MessageBuffer.size(); i++)
					{
						System.out.println(serverUser.getUsername() + " > Sending GET to: " + connectedUsersHashT.get(
							GET_MessageBuffer.get(i).getDestination().getUsername()));
						this.sendMessage(MessageType.GET,
						                 GET_MessageBuffer.get(i).getSource(),
						                 GET_MessageBuffer.get(i).getDestination(),
						                 GET_MessageBuffer.get(i).getPayload());
					}
					
					System.out.println("Exiting tempThread");
					
				};
				
				if (messageBuffer.size() > 0)
				{
					new Thread(tempThread).start();
				}

//				System.out.println("------------------------------------------------------------");
				break;
			}
			case ACK:
			{
				//check which message is it being acknowledged for. sequence number, source and destination
				//foreword the ACK message to the original sender
				//remove message from messageBuffer
				System.out.println("------------------------------------------------------------");
				for (int i = 0; i < GET_MessageBuffer.size(); i++)
				{
					//check for the sequence number, source and destination
					if (GET_MessageBuffer.get(i).getSequenceNumber() == message.getSequenceNumber() &&
						GET_MessageBuffer.get(i).getSource().getUsername().equals(message.getDestination()) &&
						GET_MessageBuffer.get(i).getDestination().getUsername().equals(message.getSource()))
					{
						Message ackMessage = GET_MessageBuffer.remove(i);
						this.sendMessage(MessageType.ACK,
						                 message.getSource(),
						                 message.getDestination(),
//						                 ackMessage.getSequenceNumber());
                                         null); //temporarily don't send  the sequence number.
					}
				}
				System.out.println("------------------------------------------------------------");
				break;
			}
			case USERS:
			{
				System.out.println("------------------------------------------------------------");
				System.out.println("------------------------------------------------------------");
				break;
			}
			case CONNECT:
			{
				System.out.println("------------------------------------------------------------");
				//get the client and add it to the connectedUser vector
				User user = message.getSource();
//				connectedUser.add(user);
				connectedUsersHashT.put(user.getUsername().toString(), clientIPaddress);
				System.out.println(serverUser.getUsername() + " > " + user.getUsername() + " is now CONNECTED");
				System.out.println("\t\tIP address: " + clientIPaddress.toString());
				//broadcast the connectedUser vector to all connected users/server
				//TODO implement broadcast the newly added user to everyone
				System.out.println(serverUser.getUsername() + " > connectedUserHashT.size() : " + connectedUsersHashT.size());
				System.out.println("------------------------------------------------------------");
				break;
			}
			case DISCONNECT:
			{
				System.out.println("------------------------------------------------------------");
				//get the client and remove it from the connectedUser vector
				User user = message.getSource();
				connectedUsersHashT.remove(user.getUsername());
				System.out.println(serverUser.getUsername() + " > " + user.getUsername() + " is now DISCONNECTED");
				System.out.println(serverUser.getUsername() + " > connectedUserHashT.size() : " + connectedUsersHashT.size());
				System.out.println("------------------------------------------------------------");

//				for (int i = 0; i < connectedUser.size(); i++)
//				{
////					if (connectedUser.get(i).getUsername().equals(user.getUsername()))
//					if (connectedUsersHashT.get(user.getUsername().toString()) != null)
//					{
////						connectedUser.remove(i);
//						connectedUsersHashT.remove(user.getUsername());
//						System.out.println(serverUser.getUsername() + " > " + user.getUsername() + "is now DISCONNECTED");
//					}
//				}
				//broadcast the connectUser vector to all connected users/server
				//TODO implement broadcast the updated userList vector to everyone
				break;
			}
			default:
			{
				System.out.println(serverUser.getUsername() + " > unknown message type from: " + message.getSource()
					.getUsername());
				break;
			}
		}
	}
	
	private class ClientThread extends Thread
	{
		Socket             socket;
		ObjectOutputStream sOutput;
		ObjectInputStream  sInput;
		private User user;
		
		
		ClientThread(Socket socket) throws
		                            IOException,
		                            ClassNotFoundException
		{
			this.socket = socket;
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			sInput = new ObjectInputStream(socket.getInputStream());
			//the first thing is the user object information.
//			user = (User) sInput.readObject();
			Message msg = (Message) sInput.readObject();
			user = msg.getSource();
			System.out.println("User: " + user.getUsername() + " connected.");
			
		}
		
		protected User getUser()
		{
			return user;
		}
		
		//this will run for every user.
		@Override
		public void run()
		{
			//keep looping until logout
			//  get the message
			//  switch message type
			//      SEND
			//          store message in messaegBuffer
			//      GET
			//          find the message destined to the user. and send.
			//          be sure to send it in the correct order
			//      USER
			//
			//      ACK
			//          send an ACK message to the original sender of the message.
			//      DISCONNECT
			//          close connection from server side.
			//          logoffUser from userList.
			//          broadcast the updated userList.
			
			boolean keepGoing = true;
			while (keepGoing)
			{
				try
				{
					msg = (Message) sInput.readObject();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
				
				switch (msg.getMessageType())
				{
					//if client A is sending a message to client B
					//TODO test whether SEND message type works.
					case SEND:
					{
						//store the message in messageBuffer vector
						System.out.print(serverUser.getUsername().toString() + " > ");
						System.out.println("Message received from: '" + msg.getSource().getUsername() + "'"
							                   + " content: '" + msg.getPayload().toString() + "'");
//						System.out.println("Message content: " + msg.getPayload().toString());
						messageBuffer.add(msg);
						System.out.println(
							serverUser.getUsername() + " > message stored in buffer. buffer size: " + messageBuffer.size());
						break;
					}//END case SEND
					//if client B is requesting messages destined to client B (receiver)
					//TODO test whether GET message type works.
					case GET:
					{
						//find all the messages destined to client B
						//group them by the sender
						//sort them by sequence for each group
						//send the message/s
						
						System.out.println(
							serverUser.getUsername().toString() + " > GET request received from: " + msg.getSource()
								.getUsername());
						
						Vector<Message> msgList = findMessages(msg.getSource());
						Message message = new Message(MessageType.GET, msg.getSource(), msg.getSource(),
						                              msgList);
						try
						{
							sOutput.writeObject(message);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						break;
					}//END case GET
					//if client B is acknowledging message has been received
					//TODO test whether ACK message type works.
					case ACK:
					{
						//forward the ACK message to client A (original sender)
						//logoffUser message from messageBuffer. Based on sender and sequence number
						
						ClientThread destination = findUser(msg.getDestination());
						try
						{
							Message message = new Message(MessageType.ACK, msg.getSource(), msg.getDestination(), null);
//							destination.sOutput.writeObject(msg);
							//forward the sequence number as well. NOT done at the moment
							destination.sOutput.writeObject(message);
							//TODO implement a proper replacement for clearing messages that are acknowledged.
							messageBuffer.clear();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						
						removeMessage(msg);
						
						break;
					}//END case ACK
					//if a server is sending information about the userList vector
					//  usually when a new client has connected or
					//  a client has disconnected.
					//TODO implement when a message is received with a USERS messageType
					case USERS:
					{
						//update the userList
						//broadcast the message to the connected clients/servers other than the one
						//it came from.
						break;
					}//END case USERS
					//if client A is disconnecting the server.
					//TODO broadcast the updated userList to every connected user/server.
					case DISCONNECT:
					{
						//disconnect the client
						//logoffUser from the userList
						//broadcast the updated userList to all connected clients/servers
						System.out.println(serverUser.getUsername().toString() + " > user '" +
							                   msg.getSource().getUsername().toString() + "' is disconnecting");
						keepGoing = false;
						logoffUser(msg.getSource());
						System.out.println(serverUser.getUsername()
							                   .toString() + " > userList.size() : " + clientThreadList.size());
						try
						{
							this.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						break;
					}//END case DISCONNECT
				}//END switch(msg.getMEssageType())
			}//END while(keepGoing)
		}//END METHOD run()
		
		//TODO catch exceptions and handle them
		private void close() throws
		                     IOException
		{
			sOutput.close();
			sInput.close();
			socket.close();
		}
		
		//TODO implement the proper writeMessage
		private boolean writeMsg(String message) throws
		                                         IOException
		{
			//check if the client is still connected.
			if (!socket.isConnected())
			{
				close();
				return false;
			}
			
			sOutput.writeObject(message);
			return true;
		}//END METHOD writeMsg(String)
	}//END INNER CLASS ClientThread
}//END CLASS Server
