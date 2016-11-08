package server.backend;

import client.backend.Message;
import client.backend.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Vector;


public class Server
{
	private int                  port;
	private SimpleDateFormat     sdf;
	private ServerSocket         serverSocket;
	private boolean              keepGoing;
	private Vector<ClientThread> clientThreadList;
	private Vector<Message>      messageBuffer;

	Server(int port)
	{
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		clientThreadList = new Vector<ClientThread>();
		messageBuffer = new Vector<Message>();
	}

	public void startServer() throws
	                          IOException,
	                          ClassNotFoundException
	{
		serverSocket = new ServerSocket(port);
		System.out.println("Server up and running");
		keepGoing = true;

		while (keepGoing)
		{
			//accept the socket
			Socket socket = serverSocket.accept();

			//for stoping the server
			if (!keepGoing)
			{
				break;
			}

			ClientThread t = new ClientThread(socket);
			clientThreadList.add(t);
//			t.start();

		}

		//close the server socket
		serverSocket.close();
		for (int i = 0; i < clientThreadList.size(); i++)
		{
			clientThreadList.get(i).sInput.close();
			clientThreadList.get(i).sOutput.close();
			clientThreadList.get(i).socket.close();
		}

	}

	public void stopServer() throws
	                         IOException
	{
		keepGoing = false;
		new Socket("localhost", port);
	}

	//TODO for the client who has logged off
	private synchronized void remove(User user)
	{
		//update the client thread
		//broadcast the update userlist to all connected users
	}

	//TODO implement the broadcastUserList method to send every user their updated userList
	private synchronized void broadcastUserList()
	{

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
			user = (User) sInput.readObject();

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
			//          remove from userList.
			//          broadcast the updated userList.

		}//END METHOD run()

		//TODO catch exceptions and handle them
		private void close() throws
		                     IOException
		{
			sOutput.close();
			sInput.close();
			socket.close();
		}

		//TODO implement the proper writeMEssage
		private boolean writeMsg(String message) throws
		                                         IOException
		{
			if (!socket.isConnected())
			{
				close();
				return false;
			}

			sOutput.writeObject(message);
			return true;
		}


	}//END INNER CLASS ClientThread


}
