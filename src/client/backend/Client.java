package client.backend;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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

	Client(String serverIP, User user)
	{
		this(serverIP, 5555, user);
	}

	Client(String serverIP, int port, User user)
	{
		this.serverIP = serverIP;
		this.port = port;
		this.user = user;
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

	protected void sendMessage(String textMessage, User destionation)
	{
		Message message = new Message(MessageType.SEND, user, destionation, textMessage);
		//send the message to the server
	}

	private void connect()
	{
		//connect to the server.
		//send the server your info. The User object
		//  if server responds with the username is already taken,
		//      choose another username
		//not sure if the code is placed here:
		//  update the connectedUser list everytime, the server sends
		//  an update of connected users.

		try
		{
			socket = new Socket(serverIP, port);
			System.out.println("Connection accepted " + socket.getInetAddress() + ":" + this.getPort());

			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		new ListenFromServer().start();

		try
		{
			Message message = new Message(MessageType.CONNECT, user, null, null);
			sOutput.writeObject(message);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}


	}

	private void disconnect()
	{
		//send a mesage to the server that you are disconnecting.
		//disconnect the server.
		//make sure the output and input streams are closed.
		//server will remove current user from the database as connected user
	}

	private void sendGetRequest()
	{
		//send a get request every n milliseconds.
		//if the server replies, update the message list.
	}

	private class ListenFromServer extends Thread
	{

	}

}//END CLASS Client
