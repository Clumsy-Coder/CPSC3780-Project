package server.backend;

import utilities.Message;
import utilities.MessageType;
import utilities.User;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Vector;


public class Server
{
	private int port = 5555;
	private boolean         keepGoing;
	private Vector<Message> messageBuffer;
	private User            serverUser;
	
	//UDP
	private DatagramSocket udpSocket;
	private final int MAX_INCOMING_SIZE = 1024;
	private Hashtable<String, InetAddress> connectedUsersHashT;
	private Vector<Message>                GET_MessageBuffer; //for when a client sent a GET request. remove elements once ACK is recieved for each message
	private InetAddress                    clientIPaddress;
	
	/**
	 * Server starts in port 5555
	 */
	Server(String username)
	{
		this(username, 5555);
		
	}//END DEFAULT CONSTRUCTOR Server(String)
	
	/**
	 * Server starts on specified port
	 *
	 * @param port
	 */
	Server(String username, int port)
	{
		serverUser = new User(username);
		this.port = port;
		messageBuffer = new Vector<Message>();
		GET_MessageBuffer = new Vector<Message>();
		connectedUsersHashT = new Hashtable<String, InetAddress>();
		
	}//END CONSTRUCTOR Server(String, port)
	
	public void startServer()
	{
		try
		{
			udpSocket = new DatagramSocket(port);
			System.out.println("Server up and running on port: " + Inet4Address.getLocalHost()
				.getHostAddress() + ":" + port);
			keepGoing = true;
			while (keepGoing)
			{
				if (!keepGoing)
				{
					udpSocket.close();
					System.out.println(serverUser.getUsername() + " > Server stopped.");
					return;
				}
				
				Message message = this.readMessage();
				//handle the message if the Message object is not null
				if (message != null)
				{
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
		
	}//END METHOD startServer()
	
	public void stopServer()
	{
		keepGoing = false;
		
	}//END METHOD stopServer()
	
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
			byte[] sendData = byteArrayOutputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
			                                               connectedUsersHashT.get(destination.getUsername()),
			                                               port);
			
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
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println(serverUser.getUsername() + " > unable to recieve packet or read object");
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return message;
		
	}//END METHOD readMessage()
	
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
				//get the message and store it in messageBuffer
				messageBuffer.add(message);
				break;
				
			}//END CASE SEND
			
			case GET:
			{
				//get the message that is destined to to client B
				//send the message to client B
				
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
						System.out.println(serverUser.getUsername() + " > Sending GET to: " + connectedUsersHashT.get(
							GET_MessageBuffer.get(i).getDestination().getUsername()));
						this.sendMessage(MessageType.GET,
						                 GET_MessageBuffer.get(i).getSource(),
						                 GET_MessageBuffer.get(i).getDestination(),
						                 GET_MessageBuffer.get(i).getPayload());
					}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
					
				};//END Lambda Runnable FUNCTION
				
				if (messageBuffer.size() > 0)
				{
					new Thread(tempThread).start();
					
				}//END if(messageBuffer.size() > 0)
				
				break;
				
			}//END CASE GET
			
			case ACK:
			{
				//check which message is it being acknowledged for. sequence number, source and destination
				//foreword the ACK message to the original sender
				//remove message from messageBuffer
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
					
				}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
				
				break;
				
			}//END CASE ACK
			
			case USERS:
			{
				System.out.println("------------------------------------------------------------");
				//send the hashtable to the connected clients, if there are more than 2 clients connected.
				if (connectedUsersHashT.size() >= 2)
				{
					
				}
				
				System.out.println("------------------------------------------------------------");
				break;
				
			}//END CASE USERS
			
			case CONNECT:
			{
				//get the client and add it to the connectedUser vector
				User user = message.getSource();
				connectedUsersHashT.put(user.getUsername(), clientIPaddress);
				//broadcast the connectedUser vector to all connected users/server
				//TODO implement broadcast the newly added user to everyone
				break;
				
			}//END CASE CONNECT
			
			case DISCONNECT:
			{
				//get the client and remove it from the connectedUser vector
				User user = message.getSource();
				connectedUsersHashT.remove(user.getUsername());
				//broadcast the connectUser vector to all connected users/server
				//TODO implement broadcast the updated userList vector to everyone
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
