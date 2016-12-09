package client.backend;

import utilities.Conversation;
import utilities.User;

import java.util.Scanner;

/**
 * Class used for running Client class
 * Takes 4 arguments
 * Username
 * server IP address
 * server port number
 * client port number
 */
public class ClientMain
{
	public static void main(String[] args)
	{
		int    serverPort        = 5555;
		String serverIP = "localhost";
		String username = "user1";
		int    clientPort        = 8000;
		String recipient = "";
		Client client = null;
		
		//if the arguments are
		//  username
		//  connecting server IP
		//  connecting server port number
		//  client port number
		if (args.length == 4)
		{
			try
			{
				username = args[0];
				serverIP = args[1];
				serverPort = Integer.parseInt(args[2]);
				clientPort = Integer.parseInt(args[3]);
				
				client = new Client(serverIP, serverPort, clientPort, new User(username));
				
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid input for server port number OR client port number");
				System.out.println("Must be an integer");
				System.out.println("Exiting");
				System.exit(-1);
			}
			
		}//END if (args.length == 4)
		
		else
		{
			System.out.println("Invalid number of arguments. Need 4 arguments");
			System.out.println("<username> <server IP address> <server port number> <client port number>");
			return;
			
		}//END ELSE FOR if (args.length == 4)
		
		
		if (!client.connect())
		{
			System.out.println("Unable to connect to server " + serverIP + ":" + serverPort);
			System.out.println("Exiting");
			return;
			
		}//END if (!client.connect())
		
		Scanner      scan      = new Scanner(System.in);
		boolean      keepGoing = true;
		Conversation curConv   = null;
		while (keepGoing)
		{
			if (!keepGoing)
			{
				break;
				
			}//END if (!keepGoing)
			
			if (curConv != null)
			{
				System.out.print(client.getUser().getUsername() + " : " + curConv.getRecipient().getUsername() + " > ");
				
			}//END if (curConv != null)
			
			else
			{
				System.out.print(username + "> ");
				
			}//END ELSE FOR if (curConv != null)
			
			String line = scan.nextLine();
			//if logout was typed
			if (line.equalsIgnoreCase("LOGOUT"))
			{
				//send a message to the server that you are disconnecting.
				keepGoing = false;
				System.out.println("Logged off");
				
			}//END if (line.equalsIgnoreCase("LOGOUT"))
			
			else if (line.equalsIgnoreCase("WHOISIN"))
			{
				client.whosConnected();
				
			}//END else if (line.equalsIgnoreCase("WHOISIN"))
			
			else
			{
				//check if the input is the username of the client.
				if (client.isRecipient(line))
				{
					curConv = client.getConversation(line);
					recipient = line;
					continue;
					
				}//END if (client.isRecipient(line))
				
				else
				{
					curConv = client.getConversation(recipient);
					if (curConv == null)
					{
						System.out.println(client.getUser().getUsername() + " > Please define the recipient. Type whoisin");
						
					}//END if (curConv == null)
					
					else
					{
						client.sendMessage(line, curConv.getRecipient());
						
					}//END ELSE FOR if (curConv == null)
					
				}//END ELSE FOR if (client.isRecipient(line))
				
			}//END ELSE
			
		}//END while (keepGoing)
		
		client.disconnect();
		
	}//END METHOD main(String [] args)
	
}//END CLASS ClientMain
