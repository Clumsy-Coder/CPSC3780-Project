package client.backend;

import utilities.Message;
import utilities.MessageType;
import utilities.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Class used to communicate with the server
 */
public class Client
{
	private static String             serverIP;
	private static User               user;
	private        int                port;
	private        ArrayList<User>    connectedUsers;
	private        ObjectOutputStream sOutput;  // I/O
	private        ObjectInputStream  sInput;   // I/O
	private        Socket             socket;   // I/O
	private        boolean            keepGoing;

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
		try
		{
			sOutput.writeObject(message);
		}
		catch (IOException e)
		{
//			e.printStackTrace();
			System.out.println("Unable to send message from : " + message.getSource().getUsername().toString());
			System.out.println("Message content: " + message.getPayload().toString());
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
		try
		{
			socket = new Socket(serverIP, port);
			System.out.println("Connection accepted " + socket.getInetAddress() + ":" + this.getPort());

			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());

		}
		catch (IOException e)
		{
//			e.printStackTrace();
			System.out.println("Could not setup socket, input stream or output stream");
			keepGoing = false;
			return keepGoing;
		}

		new ListenFromServer().start();
		System.out.println("Listening to the server");
		new SendGET_request().start();
		System.out.println("SendGET_request thread started");

		try
		{
			Message message = new Message(MessageType.CONNECT, user, null, null);
			sOutput.writeObject(message);
		}
		catch (IOException e)
		{
//			e.printStackTrace();
			System.out.println("Unable to send initial message. Disconnecting");
			disconnect();
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
		keepGoing = false;
		Message message = new Message(MessageType.DISCONNECT, user, null, null);
		try
		{
			//send a message to the server that you are disconnecting.
			sOutput.writeObject(message);
			//close the I/O streams
			if (sInput != null)
			{
				sInput.close();
			}
			if (sOutput != null)
			{
				sOutput.close();
			}
			if (socket != null)
			{
				socket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void sendGetRequest()
	{
		//send a get request every n milliseconds.
		//if the server replies, update the message list.
	}

	private class ListenFromServer extends Thread
	{
		public void run()
		{
			while (keepGoing)
			{
				try
				{
					Message msg = (Message) sInput.readObject();

					//check what type of message it is.
					//possible types received:
					//  GET
					//  ACK
					//  USERS

					switch (msg.getMessageType())
					{
						case GET:
						{
							//System.out.println("\n" + user.getUsername() + " > " + "GET request replied");
							Vector<Message> msgList = (Vector<Message>) msg.getPayload();
							for (int i = 0; i < msgList.size(); i++)
							{
								System.out.println(
										msg.getSource().getUsername() + " > " + msgList.get(i).getPayload() + "\n");
							}

							if (msgList.size() > 0)
							{
								//send a ACK
								Message message = new Message(MessageType.ACK, user, msg.getSource(), null);
								sOutput.writeObject(message);

							}

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

				}
				catch (IOException e)
				{
//					e.printStackTrace();
					System.out.println(user.getUsername() + " > Connection closed. Exiting");
					keepGoing = false;
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private class SendGET_request extends Thread
	{
		public void run()
		{
			while (keepGoing)
			{
				Message message = new Message(MessageType.GET, user, null, null);
				try
				{
					Thread.sleep(2000);
					sOutput.writeObject(message);
				}
				catch (IOException e)
				{
//					e.printStackTrace();
					System.out.println("from SendGET_request: closing thread.");
				}
				catch (InterruptedException e)
				{
//					e.printStackTrace();
					System.out.println("from SendGET_request: Thread.sleep() failed");
				}
			}
		}
	}

}//END CLASS Client
