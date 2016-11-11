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
		int    port     = 5555;
		String serverIP = "192.168.2.12";
		String username;
		String recipient = "";
		
		if(args.length == 3)
		{
			username = args[0];
			serverIP = args[1];
		    port = Integer.parseInt(args[2]);
		}
		
		else if(args.length == 4)
		{
			username = args[0];
			serverIP = args[1];
			port = Integer.parseInt(args[2]);
			recipient = args[3];
		}
		
		else
		{
			System.out.println("Invalid number of arguments. Need 3 arguments");
			System.out.println("Username serverIP port");
			return;
		}

		User   user   = new User(username);
		Client client = new Client(serverIP, port, user);

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
			else
			{
				client.sendMessage(line, new User(recipient));
				
			}
		}
		client.disconnect();

	}
}
