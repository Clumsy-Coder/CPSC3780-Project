package server.backend;

import utilities.Message;
import utilities.MessageType;
import utilities.User;
import utilities.UserNetworkInfo;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


public class Server
{
	private int serverPort = 5555;
	private boolean         keepGoing;
	private Vector<Message> messageBuffer;
	private User            serverUser;
	private int             clientPort;
	
	//UDP
	private DatagramSocket udpSocket;
	private final int MAX_INCOMING_SIZE = 1024;
	private Vector<UserNetworkInfo> connectedUsersHashT;
	private Vector<Message>         GET_MessageBuffer; //for when a client sent a GET request. remove elements once ACK is recieved for each message
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
		connectedUsersHashT = new Vector<UserNetworkInfo>();
		
	}//END CONSTRUCTOR Server(String, serverPort)
	
	public void startServer()
	{
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
	private synchronized void broadcastUserList(Message message)
	{
//		Vector<NetworkInfo> broadCastVector = new Vector<>(connectedUsersHashT.values());
		Iterator <UserNetworkInfo> it = connectedUsersHashT.iterator();
		//send the message to all clients connected to the server.
		while (it.hasNext())
		{
//			Map.Entry<String, UserNetworkInfo> pair = (Map.Entry) it.next();
			Message broadcastMessage = new Message(message.getMessageType(),
			                                       serverUser,
			                                       it.next().getUser(),
			                                       message.getPayload());
			this.sendMessage(broadcastMessage);
			it.remove();
			
		}
	}
	
	private synchronized UserNetworkInfo getClient(User user)
	{
		UserNetworkInfo connectedClient = null;
		
		for(int i = 0; i < connectedUsersHashT.size(); i++)
		{
			if(connectedUsersHashT.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				connectedClient = connectedUsersHashT.get(i);
				return connectedClient;
			}
		}
		
		return connectedClient;
	}
	
	private synchronized void removeClient(User user)
	{
		for(int i = 0; i < connectedUsersHashT.size(); i++)
		{
			if(connectedUsersHashT.get(i).getUser().getUsername().equals(user.getUsername()))
			{
				connectedUsersHashT.remove(i);
				return;
			}
		}
	}
	
	//	private synchronized void sendMessage(MessageType messageType, User source, User destination, Object payload)
	private synchronized void sendMessage(Message sendMessage)
	{
//		Message message = new Message(messageType, source, destination, payload);
		
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream    oos                   = new ObjectOutputStream(byteArrayOutputStream);
			oos.writeObject(sendMessage);
			byte[] sendData = byteArrayOutputStream.toByteArray();
//			InetAddress destinationIP = connectedUsersHashT.get(sendMessage.getDestination()
//				                                                    .getUsername()).getIpAddress();
			InetAddress destinationIP = this.getClient(sendMessage.getDestination()).getIpAddress();
			int destinationPort = this.getClient(sendMessage.getDestination()).getPort();
//			int destinationPort = connectedUsersHashT.get(sendMessage.getDestination().getUsername()).getPort();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
			                                               sendData.length,
//			                                               connectedUsersHashT.get(sendMessage.getDestination()
//				                                                                              .getUsername())
//				                                                                              .getIpAddress(),
                                                           destinationIP,
//			                                               connectedUsersHashT.get(sendMessage.getDestination()
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
				System.out.println("\t\tMessage: " + message.getPayload().toString());
				System.out.println("------------------------------------------------------------");
				break;
				
			}//END CASE SEND
			
			case GET:
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
						
						System.out.println(serverUser.getUsername() + " > Sending GET to: " + this.getClient(
							GET_MessageBuffer.get(i).getDestination()).getIpAddress());
						Message getMessage = new Message(MessageType.GET,
						                                 GET_MessageBuffer.get(i).getSource(),
						                                 GET_MessageBuffer.get(i).getDestination(),
						                                 GET_MessageBuffer.get(i).getPayload());
//						this.sendMessage(MessageType.GET,
//						                 GET_MessageBuffer.get(i).getSource(),
//						                 GET_MessageBuffer.get(i).getDestination(),
//						                 GET_MessageBuffer.get(i).getPayload());
						this.sendMessage(getMessage);
					}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
					
				};//END Lambda Runnable FUNCTION
				
				if (messageBuffer.size() > 0)
				{
					new Thread(tempThread).start();
					
				}//END if(messageBuffer.size() > 0)
				
				System.out.println("------------------------------------------------------------");
				break;
				
			}//END CASE GET
			
			case ACK:
			{
				//message format received
				//  Type: ACK
				//  source: source (the sender)
				//  destination: destination (who is the sender sending to)
				//  payload: sequence number
				
				System.out.println("------------------------------------------------------------");
				System.out.println(serverUser.getUsername() + " > ACK from: " + message.getSource().getUsername());
				System.out.println("\t\tTo: " + message.getDestination().getUsername());
				System.out.println("\t\tSequence nunmber: " + message.getPayload() != null ?
				                   message.getPayload().toString() :
				                   null);
				//check which message is it being acknowledged for. sequence number, source and destination
				//foreword the ACK message to the original sender
				//remove message from messageBuffer
				for (int i = 0; i < GET_MessageBuffer.size(); i++)
				{
					//message format sent
					//  Type: ACK
					//  source: source (the one who sent ACK message)
					//  destination: destination (the one who sent the SEND message)
					//  payload: sequence number
					
					//check for the sequence number, source and destination
					if (GET_MessageBuffer.get(i)
						.getSequenceNumber()
						.compareTo((BigInteger) message.getPayload()) == 0 &&
						GET_MessageBuffer.get(i).getSource().getUsername().equals(message.getDestination()) &&
						GET_MessageBuffer.get(i).getDestination().getUsername().equals(message.getSource()))
					{
						Message ackMessage = GET_MessageBuffer.remove(i);
						
						this.sendMessage(new Message(MessageType.ACK,
						                             message.getSource(),
						                             message.getDestination(),
						                             ackMessage.getSequenceNumber()));
//                                                     null)); //temporarily don't send  the sequence number.
					}
					
				}//END for (int i = 0; i < GET_MessageBuffer.size(); i++)
				
				System.out.println("------------------------------------------------------------");
				
				break;
				
			}//END CASE ACK
			
			case USERS:
			{
				System.out.println("------------------------------------------------------------");
				//let everyone know who just connected to the server.
				
				//message format received:
				//  Type: USERS
				//  source: server
				//  destination: user (who are connected)
				//  payload: User object (must contain initial sequence num)
				//  NOTE: must contain sequence number.
				
				if (connectedUsersHashT.size() >= 2)
				{
					
				}
				
				System.out.println("------------------------------------------------------------");
				break;
				
			}//END CASE USERS
			
			case CONNECT:
			{
				//message format recieved
				//  Type: CONNECT
				//  source: source (the one who is connecting)
				//  destination: server
				//  payload: User object
				//  NOTE: must contain sequence number
				
				System.out.println("------------------------------------------------------------");
				//get the client and add it to the connectedUser vector
				User            user         = message.getSource();
				UserNetworkInfo clientIPInfo = new UserNetworkInfo(clientIPaddress, clientPort, user);
//				connectedUsersHashT.put(user.getUsername(), clientIPaddress);
				System.out.println(serverUser.getUsername() + " > User: '" + user.getUsername() + "' is now CONNECTED");
				
				//todo tell the newly connected client who is currently connected
				Iterator <UserNetworkInfo> it = connectedUsersHashT.iterator();
				//send the message to all clients connected to the server.
				while (it.hasNext())
				{
//					Map.Entry<String, UserNetworkInfo> pair = (Map.Entry) it.next();
					Message broadcastMessage = new Message(MessageType.USERS,
					                                       serverUser,
					                                       it.next().getUser(),
//					                                       message.getSource(),
					                                       message.getPayload());
					this.sendMessage(broadcastMessage);
					it.remove();
					
				}
				
				//TODO implement broadcast the newly added user to everyone
				//let everyone connected to the server know that a new user has connected to the server.
				//connectedUsersHastT.size() + 1 because you don't want to broadcast to the original sender.
				//  the original sender would think thats a new user.
				if (connectedUsersHashT.size() + 1 >= 2)
				{
					//message format sent
					//  Type: USERS
					//  source: server
					//  destination: all users/servers connected
					//  payload: User object
					//  NOTE: must contain sequence number
					
					Message newClientMessage = new Message(MessageType.USERS,
					                                       message.getSource(),
					                                       null,
					                                       message.getSource());
					this.broadcastUserList(newClientMessage);
					
				}
//				connectedUsersHashT.put(user, clientIPInfo);
				connectedUsersHashT.add(clientIPInfo);
				
				System.out.println("------------------------------------------------------------");
				break;
				
			}//END CASE CONNECT
			
			case DISCONNECT:
			{
				//message format received
				//  Type: DISCONNECT
				//  source: source (the sender)
				//  destination: server
				//  payload: null
				
				System.out.println("------------------------------------------------------------");
				//get the client and remove it from the connectedUser vector
				User user = message.getSource();
//				connectedUsersHashT.remove(user.getUsername());
				this.removeClient(user);
				System.out.println(serverUser.getUsername() + " > User: '" + user.getUsername() + "' is now DISCONNECTED");
				//broadcast the connectUser vector to all connected users/server
				//TODO implement broadcast the updated userList vector to everyone
				//let everyone know who just disconnected
				if (connectedUsersHashT.size() >= 1)
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
