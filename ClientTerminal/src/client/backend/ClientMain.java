package client.backend;

import utilities.Conversation;
import utilities.User;

import java.util.Scanner;

/**
 * Created by Umar on 2016-11-08.
 */
public class ClientMain
{
	public static void main(String[] args)
	{
		int    serverPort        = 5555;
		String serverIP          = "192.168.2.12";
		String username;
		int    clientPort        = 8000;
		
		Client client;
		User   user;
		
		if (args.length == 4)
		{
			username = args[0];
			serverIP = args[1];
			serverPort = Integer.parseInt(args[2]);
			clientPort = Integer.parseInt(args[3]);
			
			user = new User(username);
			client = new Client(serverIP, serverPort, clientPort, user);
			
		}
		
		else
		{
			System.out.println("Invalid number of arguments. Need 5 arguments");
			System.out.println("Username serverIP serverPort recipientUsername clientPort");
			return;
		}
		
		if (!client.connect())
		{
			return;
		}
		
		Scanner      scan      = new Scanner(System.in);
		boolean      keepGoing = true;
		Conversation curConv   = null;
		while (keepGoing)
		{
			if (!keepGoing)
			{
				break;
			}
			
			
			if (curConv != null)
			{
				System.out.print(user.getUsername() + " : " + curConv.getRecipient().getUsername() + " > ");
			}
			else
			{
				System.out.print(username + "> ");
			}
			
			String line = scan.nextLine();
			//if logout was typed
			if (line.equalsIgnoreCase("LOGOUT"))
			{
				//send a message to the server that you are disconnecting.
				keepGoing = false;
				System.out.println("Logged off");
//				client.disconnect();
				
			}
			else if (line.equalsIgnoreCase("WHOISIN"))
			{
				client.whosConnected();
			}
			else
			{
				//check if the input is the username of the client.
				if (client.isRecipient(line))
				{
					curConv = client.getConversation(line);
					continue;
				}
				
				else
				{
					if (curConv == null)
					{
						System.out.println(user.getUsername() + " > Please define the recipient. Type whoisin");
					}
					else
					{
						client.sendMessage(line, curConv.getRecipient());
						
					}
				}
				
			}
		}
		client.disconnect();
		
	}
}
