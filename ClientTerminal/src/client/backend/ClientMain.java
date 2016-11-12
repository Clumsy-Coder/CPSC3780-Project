package client.backend;

import utilities.User;

import java.util.Scanner;

/**
 * Created by Umar on 2016-11-08.
 */
public class ClientMain
{
	public static void main(String[] args)
	{
		int    serverPort     = 5555;
		String serverIP = "192.168.2.12";
		String username;
		String recipientUsername = "";
		int clientPort = 8000;
		
		Client client;
		User user;
		User recipient;
		
		if(args.length == 5)
		{
			username = args[0];
			serverIP = args[1];
			serverPort = Integer.parseInt(args[2]);
			recipientUsername = args[3];
			clientPort = Integer.parseInt(args[4]);
			
			user = new User(username);
			recipient = new User(recipientUsername);
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

		Scanner scan      = new Scanner(System.in);
		boolean keepGoing = true;
		while (keepGoing)
		{
			if(!keepGoing)
			{
				break;
			}
			System.out.print(username + "> ");
			String line = scan.nextLine();
			//if logout was typed
			if (line.equalsIgnoreCase("LOGOUT"))
			{
				//send a message to the server that you are disconnecting.
				keepGoing = false;
				System.out.println("Logged off");
//				client.disconnect();

			}
			else if(line.equals("WHOISIN"))
			{
				client.whosConnected();
			}
			else
			{
				client.sendMessage(line, recipient);
				
			}
		}
		client.disconnect();

	}
}
